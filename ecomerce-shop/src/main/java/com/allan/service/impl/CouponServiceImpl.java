package com.allan.service.impl;

import com.allan.domain.CouponStatus;
import com.allan.dto.CartContext;
import com.allan.dto.CouponCreateRequest;
import com.allan.dto.PromotionEvaluationResult;
import com.allan.dto.RuleEvaluationOutcome;
import com.allan.exceptions.CouponNotFoundException;
import com.allan.exceptions.CouponRedemptionException;
import com.allan.exceptions.PromotionNotFoundException;
import com.allan.model.Coupon;
import com.allan.model.Promotion;
import com.allan.repository.CouponRedemptionRepository;
import com.allan.repository.CouponRepository;
import com.allan.repository.PromotionRepository;
import com.allan.service.CouponService;
import com.allan.service.PromotionAuditService;
import com.allan.service.RuleEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static com.allan.exceptions.CouponRedemptionException.Reason.*;

/**
 * See {@link CouponService} for the full security contract. Key points
 * enforced here specifically:
 * <ul>
 *   <li>{@link #normalizeCode(String)} is the ONLY place code normalization
 *       happens — every other method routes through it before touching the
 *       repository.</li>
 *   <li>Bulk-generated codes use {@link SecureRandom}/{@code UUID.randomUUID()}
 *       — never a predictable counter — and collisions are checked and
 *       retried, not just assumed away.</li>
 *   <li>{@link #validate(String, CartContext)} performs zero writes; see
 *       class-level notes on the interface for why that matters.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private static final int MAX_BULK_COUNT = 10_000; // guards against a single request exhausting resources
    private static final int MAX_CODE_GENERATION_ATTEMPTS = 5;

    private final CouponRepository couponRepository;
    private final PromotionRepository promotionRepository;
    private final CouponRedemptionRepository couponRedemptionRepository;
    private final RuleEngineService ruleEngineService;
    private final PromotionAuditService auditService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public Coupon createCoupon(Long promotionId, Long sellerId, CouponCreateRequest request) {
        Promotion promotion = promotionRepository.findByIdAndSellerId(promotionId, sellerId)
                .orElseThrow(() -> new PromotionNotFoundException(promotionId));

        if (request.usageLimit() == null) {
            // Per Coupon's javadoc: NULL usage limit is reserved for admin
            // platform coupons only. A seller-issued coupon must always cap
            // total redemptions.
            throw new IllegalArgumentException("Sellers must set a usageLimit (unlimited coupons are admin-only)");
        }

        String code = request.code() != null ? normalizeCode(request.code()) : generateUniqueCode();
        if (couponRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Code already in use: " + code);
        }

        Coupon coupon = buildCoupon(promotion, code, request);
        coupon = couponRepository.save(coupon);
        auditService.record(promotionId, "COUPON_GENERATED", null, coupon.getCode(), null);
        return coupon;
    }

    @Override
    @Transactional
    public Coupon createAdminCoupon(Long promotionId, CouponCreateRequest request) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new PromotionNotFoundException(promotionId));

        String code = request.code() != null ? normalizeCode(request.code()) : generateUniqueCode();
        if (couponRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Code already in use: " + code);
        }

        Coupon coupon = buildCoupon(promotion, code, request); // usageLimit may legitimately be null here
        coupon = couponRepository.save(coupon);
        auditService.record(promotionId, "COUPON_GENERATED", null, coupon.getCode(), null);
        return coupon;
    }

    @Override
    @Transactional
    public List<Coupon> bulkGenerateCoupons(Long promotionId, Long sellerId, int count, CouponCreateRequest template) {
        if (count <= 0 || count > MAX_BULK_COUNT) {
            throw new IllegalArgumentException("count must be between 1 and " + MAX_BULK_COUNT);
        }
        Promotion promotion = promotionRepository.findByIdAndSellerId(promotionId, sellerId)
                .orElseThrow(() -> new PromotionNotFoundException(promotionId));
        if (template.usageLimit() == null) {
            throw new IllegalArgumentException("Sellers must set a usageLimit (unlimited coupons are admin-only)");
        }

        List<Coupon> generated = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            // Ignore any code on the template — bulk generation always
            // mints a fresh random code per unit, per interface contract.
            String code = generateUniqueCode();
            Coupon coupon = buildCoupon(promotion, code,
                    new CouponCreateRequest(code, template.usageLimit(), template.usagePerUser()));
            generated.add(couponRepository.save(coupon));
        }
        auditService.record(promotionId, "COUPON_GENERATED", null, generated.size() + " bulk coupons", null);
        return generated;
    }

    @Override
    @Transactional
    public void disableCoupon(Long couponId, Long sellerId, String reason) {
        Coupon coupon = couponRepository.findByIdAndPromotionSellerId(couponId, sellerId)
                .orElseThrow(() -> new CouponNotFoundException(couponId));

        coupon.setStatus(CouponStatus.DISABLED);
        couponRepository.save(coupon);
        auditService.record(coupon.getPromotion().getId(), "COUPON_DISABLED", null, coupon.getCode(), reason);
    }

    @Override
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void disableCouponAsAdmin(Long couponId, String reason) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponNotFoundException(couponId));

        coupon.setStatus(CouponStatus.DISABLED);
        couponRepository.save(coupon);
        auditService.record(coupon.getPromotion().getId(), "COUPON_DISABLED", null, coupon.getCode(), reason);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Coupon> findByCode(String code) {
        return couponRepository.findByCode(normalizeCode(code));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Coupon> findByPromotionId(Long promotionId, Long sellerId, Pageable pageable) {
        if (!promotionRepository.existsByIdAndSellerId(promotionId, sellerId)) {
            throw new PromotionNotFoundException(promotionId);
        }
        return couponRepository.findByPromotionId(promotionId, pageable);
    }

    @Override
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Page<Coupon> findByPromotionIdAsAdmin(Long promotionId, Pageable pageable) {
        if (!promotionRepository.existsById(promotionId)) {
            throw new PromotionNotFoundException(promotionId);
        }
        return couponRepository.findByPromotionId(promotionId, pageable);
    }

    @Override
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Page<Coupon> findAllForAdmin(Pageable pageable) {
        return couponRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionEvaluationResult validate(String code, CartContext cart) {
        String normalized = normalizeCode(code);
        Coupon coupon = couponRepository.findByCode(normalized)
                .orElseThrow(() -> new CouponRedemptionException(INVALID_CODE, "Coupon not found"));

        if (coupon.getStatus() == CouponStatus.DISABLED) {
            throw new CouponRedemptionException(INVALID_CODE, "Coupon is disabled");
        }
        if (coupon.getStatus() == CouponStatus.EXHAUSTED || !coupon.isAvailable()) {
            throw new CouponRedemptionException(EXHAUSTED, "Coupon has reached its usage limit");
        }

        Promotion promotion = coupon.getPromotion();
        LocalDateTime now = cart.evaluatedAt();
        if (now.isBefore(promotion.getStartsAt())) {
            throw new CouponRedemptionException(NOT_YET_ACTIVE, "Coupon is not yet active");
        }
        if (!promotion.isLive()) {
            throw new CouponRedemptionException(EXPIRED, "Coupon/promotion is no longer active");
        }
        if (cart.subtotal() < promotion.getMinimumOrderValue()) {
            throw new CouponRedemptionException(BELOW_MINIMUM_ORDER_VALUE,
                    "Cart subtotal is below this promotion's minimum order value");
        }

        if (cart.userId() != null) {
            long usedByUser = couponRedemptionRepository.countByCouponIdAndUserIdAndReversedFalse(coupon.getId(), cart.userId());
            Integer perUserLimit = coupon.getUsagePerCustomer();
            if (perUserLimit != null && usedByUser >= perUserLimit) {
                throw new CouponRedemptionException(PER_USER_LIMIT_REACHED, "You've already used this coupon");
            }
        }

        List<RuleEvaluationOutcome> ruleOutcomes = ruleEngineService.evaluate(promotion, cart);
        if (!ruleEngineService.isEligible(promotion, cart)) {
            throw new CouponRedemptionException(RULES_NOT_MET, "Cart does not meet this promotion's requirements");
        }

        long discount = DiscountCalculator.compute(promotion, cart);
        return PromotionEvaluationResult.eligible(promotion.getId(), discount, ruleOutcomes);
    }

    @Override
    public String normalizeCode(String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            throw new IllegalArgumentException("Coupon code cannot be blank");
        }
        return rawCode.trim().toUpperCase(Locale.ROOT);
    }

    // ── internal helpers ─────────────────────────────────────────────────

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < MAX_CODE_GENERATION_ATTEMPTS; attempt++) {
            // UUID.randomUUID() is backed by SecureRandom on the JDKs this
            // targets; explicitly seeding our own SecureRandom-derived
            // suffix as well guards against any future JDK change to that
            // default without us noticing.
            String candidate = "PROMO-" + UUID.randomUUID().toString().replace("-", "")
                    .substring(0, 10).toUpperCase(Locale.ROOT) + secureRandom.nextInt(10);
            if (!couponRepository.existsByCode(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not generate a unique coupon code after "
                + MAX_CODE_GENERATION_ATTEMPTS + " attempts");
    }

    private Coupon buildCoupon(Promotion promotion, String code, CouponCreateRequest request) {
        Coupon coupon = new Coupon();
        coupon.setPromotion(promotion);
        coupon.setCode(code);
        coupon.setUsageLimit(request.usageLimit());
        coupon.setUsagePerCustomer(request.usagePerUser() != null ? request.usagePerUser() : 1);
        coupon.setStatus(CouponStatus.ACTIVE);
        coupon.setUsedCount(0);
        return coupon;
    }
}