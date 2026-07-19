package com.allan.service.impl;

import com.allan.domain.PromotionScope;
import com.allan.dto.CartContext;
import com.allan.dto.CartItem;
import com.allan.dto.PromotionApplicationResult;
import com.allan.dto.PromotionEvaluationResult;
import com.allan.exceptions.CouponRedemptionException;
import com.allan.model.Coupon;
import com.allan.model.Promotion;
import com.allan.repository.PromotionRepository;
import com.allan.service.CouponService;
import com.allan.service.PromotionEvaluatorService;
import com.allan.service.RuleEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * See {@link PromotionEvaluatorService} for the full security contract. Key
 * points enforced here specifically:
 * <ul>
 *   <li>{@link #findCandidatePromotions(CartContext)} only ever queries via
 *       {@code PromotionRepository.findActiveByScope}/{@code findActiveBySellerId},
 *       both of which filter to {@code status = ACTIVE} and the current
 *       time window at the SQL level — there is no code path here that can
 *       evaluate a DRAFT/PAUSED/ARCHIVED promotion.</li>
 *   <li>Tie-breaking on equal {@code priority} falls back to promotion
 *       {@code id} ascending — deterministic, not iteration-order-dependent.</li>
 *   <li><strong>Stacking/exclusivity interpretation (documented assumption):</strong>
 *       if ANY eligible candidate in the pool — primary or not — is
 *       {@code exclusive}, this implementation suppresses ALL stacking for
 *       the cart, not just stacking with that one promotion. This is the
 *       conservative reading of "exclusive overrides stackable on any other
 *       promotion" and errs toward under-discounting rather than
 *       over-discounting. Revisit with product/business rules if a more
 *       permissive interpretation is intended.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class PromotionEvaluatorServiceImpl implements PromotionEvaluatorService {

    private final PromotionRepository promotionRepository;
    private final RuleEngineService ruleEngineService;
    private final CouponService couponService;

    @Override
    @Transactional(readOnly = true)
    public PromotionApplicationResult evaluate(CartContext cart) {
        List<Promotion> candidates = findCandidatePromotions(cart).stream()
                .filter(p -> !p.isCodeBased()) // automatic promotions only — coupon-gated ones need evaluateWithCoupon
                .sorted(priorityThenId())
                .toList();

        return resolve(candidates, cart, null);
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionApplicationResult evaluateWithCoupon(CartContext cart, String couponCode) {
        // Revalidate the coupon ourselves — never trust that the caller
        // (or cart.couponCode()) already did this correctly.
        PromotionEvaluationResult couponResult;
        try {
            couponResult = couponService.validate(couponCode, cart);
        } catch (CouponRedemptionException e) {
            // Propagate — checkout should surface "this code isn't valid"
            // rather than silently falling back to automatic promotions.
            throw e;
        }

        Optional<Coupon> couponOpt = couponService.findByCode(couponCode);
        if (couponOpt.isEmpty()) {
            throw new CouponRedemptionException(CouponRedemptionException.Reason.INVALID_CODE, "Coupon not found");
        }
        Promotion couponPromotion = couponOpt.get().getPromotion();

        List<Promotion> automaticCandidates = findCandidatePromotions(cart).stream()
                .filter(p -> !p.isCodeBased())
                .filter(p -> !p.getId().equals(couponPromotion.getId()))
                .sorted(priorityThenId())
                .toList();

        // The explicit coupon always becomes primary — the customer
        // deliberately supplied a code, so it takes precedence over
        // whatever automatic promotion would otherwise have won on priority.
        boolean anyExclusiveEligible = couponPromotion.isExclusive()
                || automaticCandidates.stream()
                    .filter(p -> ruleEngineService.isEligible(p, cart))
                    .anyMatch(Promotion::isExclusive);

        if (anyExclusiveEligible || !couponPromotion.isStackable()) {
            long discount = couponResult.discountAmount();
            return new PromotionApplicationResult(couponPromotion.getId(), List.of(), discount, couponResult.eligible() ? couponCode : null);
        }

        List<Long> stackedIds = new ArrayList<>();
        long total = couponResult.discountAmount();
        for (Promotion candidate : automaticCandidates) {
            if (!candidate.isStackable() || candidate.isExclusive()) {
                continue;
            }
            if (ruleEngineService.isEligible(candidate, cart)) {
                stackedIds.add(candidate.getId());
                total += DiscountCalculator.compute(candidate, cart);
            }
        }
        total = Math.min(total, cart.subtotal());

        return new PromotionApplicationResult(couponPromotion.getId(), stackedIds, total, couponCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Promotion> findCandidatePromotions(CartContext cart) {
        var now = cart.evaluatedAt();
        Map<Long, Promotion> byId = new LinkedHashMap<>();

        promotionRepository.findActiveByScope(PromotionScope.PLATFORM_WIDE, now)
                .forEach(p -> byId.put(p.getId(), p));

        Set<Long> sellerIds = cart.items().stream()
                .map(CartItem::sellerId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        for (Long sellerId : sellerIds) {
            promotionRepository.findActiveBySellerId(sellerId, now)
                    .forEach(p -> byId.put(p.getId(), p));
        }

        // CATEGORY/PRODUCT scoped promotions are still surfaced by seller
        // or platform-wide queries above (scope is descriptive metadata —
        // the actual restriction is enforced by PromotionRule evaluation,
        // per Promotion's own javadoc), so no separate query is needed here.

        return new ArrayList<>(byId.values());
    }

    // ── internal helpers ─────────────────────────────────────────────────

    private Comparator<Promotion> priorityThenId() {
        return Comparator.comparingInt(Promotion::getPriority).thenComparing(Promotion::getId);
    }

    private PromotionApplicationResult resolve(List<Promotion> sortedCandidates, CartContext cart, String couponCode) {
        List<Promotion> eligible = sortedCandidates.stream()
                .filter(p -> ruleEngineService.isEligible(p, cart))
                .toList();

        if (eligible.isEmpty()) {
            return PromotionApplicationResult.none();
        }

        Promotion primary = eligible.get(0);
        long total = DiscountCalculator.compute(primary, cart);

        boolean anyExclusive = eligible.stream().anyMatch(Promotion::isExclusive);
        if (anyExclusive || !primary.isStackable()) {
            return new PromotionApplicationResult(primary.getId(), List.of(), total, couponCode);
        }

        List<Long> stackedIds = new ArrayList<>();
        for (Promotion candidate : eligible.subList(1, eligible.size())) {
            if (candidate.isStackable() && !candidate.isExclusive()) {
                stackedIds.add(candidate.getId());
                total += DiscountCalculator.compute(candidate, cart);
            }
        }
        total = Math.min(total, cart.subtotal());

        return new PromotionApplicationResult(primary.getId(), stackedIds, total, couponCode);
    }
}