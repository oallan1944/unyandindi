package com.allan.service.impl;

import com.allan.domain.CouponStatus;
import com.allan.dto.CartContext;
import com.allan.dto.PromotionEvaluationResult;
import com.allan.dto.RedemptionOutcome;
import com.allan.exceptions.CouponRedemptionException;
import com.allan.model.Coupon;
import com.allan.model.CouponRedemption;
import com.allan.repository.CouponRedemptionRepository;
import com.allan.repository.CouponRepository;
import com.allan.service.CouponService;
import com.allan.service.PromotionAuditService;
import com.allan.service.RedemptionService;
import com.allan.service.support.DistributedLockProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.allan.exceptions.CouponRedemptionException.Reason.*;

/**
 * See {@link RedemptionService} for the full security contract. Key points
 * enforced here specifically — the three-layer concurrency model:
 * <ol>
 *   <li>{@link DistributedLockProvider} (Redis in production) locked on the
 *       normalized code, acquired first and held for the shortest possible
 *       window.</li>
 *   <li>{@code CouponRepository.findWithLockByCode} — a DB pessimistic
 *       write lock — as the fallback inside the same transaction.</li>
 *   <li>The unique DB index on {@code coupon_redemptions.order_id} as the
 *       final guard: a constraint violation here is caught and translated
 *       to {@code ORDER_ALREADY_REDEEMED} rather than surfacing as a raw
 *       {@code DataIntegrityViolationException}.</li>
 * </ol>
 * Every redemption re-runs full validation ({@code CouponService.validate})
 * against a freshly supplied {@code CartContext} inside the lock — the
 * discount actually charged is always the one computed at THIS moment, never
 * a value computed earlier during a cart preview or passed in by a caller.
 */
@Service
@RequiredArgsConstructor
public class RedemptionServiceImpl implements RedemptionService {

    private static final Logger log = LoggerFactory.getLogger(RedemptionServiceImpl.class);

    private static final Duration LOCK_WAIT = Duration.ofSeconds(5);
    private static final Duration LOCK_LEASE = Duration.ofSeconds(10);
    private static final String LOCK_KEY_PREFIX = "coupon-lock:";

    private final CouponRepository couponRepository;
    private final CouponRedemptionRepository couponRedemptionRepository;
    private final CouponService couponService;
    private final PromotionAuditService auditService;
    private final DistributedLockProvider lockProvider;

    @Override
    @Transactional
    public RedemptionOutcome redeem(String couponCode, Long userId, Long orderId, CartContext cart) {
        if (userId == null || orderId == null) {
            throw new IllegalArgumentException("userId and orderId are required");
        }
        String normalizedCode = couponService.normalizeCode(couponCode);

        // Idempotency guard #0, before we even touch the coupon: an order
        // can never receive two redemptions.
        if (couponRedemptionRepository.existsByOrderId(orderId)) {
            throw new CouponRedemptionException(ORDER_ALREADY_REDEEMED, "This order has already used a coupon");
        }

        DistributedLockProvider.LockHandle lock = lockProvider.tryLock(LOCK_KEY_PREFIX + normalizedCode, LOCK_WAIT, LOCK_LEASE);
        if (lock == null) {
            throw new CouponRedemptionException(LOCK_TIMEOUT, "System is busy processing this coupon, please retry");
        }

        try {
            // DB pessimistic write lock — second layer, inside the same transaction.
            Coupon coupon = couponRepository.findWithLockByCode(normalizedCode)
                    .orElseThrow(() -> new CouponRedemptionException(INVALID_CODE, "Coupon not found"));

            // Re-validate fully and fresh — never trust anything computed
            // earlier (e.g. during cart preview) or supplied by the caller.
            PromotionEvaluationResult result = couponService.validate(normalizedCode, cart);

            // Explicit per-user re-check even though validate() already did
            // this — defense in depth against any future refactor of
            // validate() that might loosen this check.
            long usedByUser = couponRedemptionRepository.countByCouponIdAndUserIdAndReversedFalse(coupon.getId(), userId);
            Integer perUserLimit = coupon.getUsagePerCustomer();
            if (perUserLimit != null && usedByUser >= perUserLimit) {
                throw new CouponRedemptionException(PER_USER_LIMIT_REACHED, "You've already used this coupon");
            }

            long discount = Math.min(result.discountAmount(), cart.subtotal()); // redundant clamp, cheap insurance

            coupon.setUsedCount(coupon.getUsedCount() + 1);
            boolean nowExhausted = coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit();
            if (nowExhausted) {
                coupon.setStatus(CouponStatus.EXHAUSTED);
            }
            couponRepository.save(coupon);

            CouponRedemption redemption = new CouponRedemption();
            redemption.setCoupon(coupon);
            redemption.setUserId(userId);
            redemption.setOrderId(orderId);
            redemption.setDiscount(discount);
            redemption.setCartTotalAtRedemption(cart.subtotal());
            redemption.setReversed(false);

            try {
                redemption = couponRedemptionRepository.save(redemption);
            } catch (DataIntegrityViolationException e) {
                // Final guard: unique constraint on order_id caught a race
                // that slipped past the two locks above.
                throw new CouponRedemptionException(ORDER_ALREADY_REDEEMED, "This order has already used a coupon");
            }

            Long promotionId = coupon.getPromotion().getId();
            auditService.recordSystemAction(promotionId, "COUPON_REDEEMED", null,
                    "order#" + orderId + " discount=" + discount, null, "REDEMPTION_SERVICE");
            if (nowExhausted) {
                auditService.recordSystemAction(promotionId, "COUPON_EXHAUSTED", null, coupon.getCode(),
                        "usedCount reached usageLimit", "REDEMPTION_SERVICE");
            }

            return new RedemptionOutcome(redemption.getId(), coupon.getId(), promotionId, discount, cart.subtotal());
        } finally {
            lock.close();
        }
    }

    @Override
    @PreAuthorize("hasAnyRole('SYSTEM','ADMIN')")
    @Transactional
    public RedemptionOutcome reverse(Long orderId, String actor) {
        // NOTE: this method must only ever be invoked by the order-cancellation
        // workflow or an admin action — see interface javadoc. The
        // @PreAuthorize above is defense-in-depth; the real boundary is that
        // no customer-facing endpoint should ever call this directly.
        CouponRedemption redemption = couponRedemptionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("No redemption found for order " + orderId));

        if (redemption.isReversed()) {
            // Idempotent no-op — a retry of a reversal should not double-decrement usedCount.
            Coupon coupon = redemption.getCoupon();
            return new RedemptionOutcome(redemption.getId(), coupon.getId(), coupon.getPromotion().getId(),
                    redemption.getDiscount(), redemption.getCartTotalAtRedemption());
        }

        redemption.setReversed(true);
        redemption.setReversedAt(LocalDateTime.now());
        couponRedemptionRepository.save(redemption);

        Coupon coupon = redemption.getCoupon();
        coupon.setUsedCount(Math.max(0, coupon.getUsedCount() - 1));
        if (coupon.getStatus() == CouponStatus.EXHAUSTED
                && (coupon.getUsageLimit() == null || coupon.getUsedCount() < coupon.getUsageLimit())) {
            coupon.setStatus(CouponStatus.ACTIVE);
        }
        couponRepository.save(coupon);

        Long promotionId = coupon.getPromotion().getId();
        auditService.recordSystemAction(promotionId, "REDEMPTION_REVERSED", null, null,
                "Reversed redemption for order #" + orderId + " by " + actor, "REDEMPTION_SERVICE");

        log.info("Redemption for order {} reversed by {}", orderId, actor);

        return new RedemptionOutcome(redemption.getId(), coupon.getId(), promotionId,
                redemption.getDiscount(), redemption.getCartTotalAtRedemption());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserRedeemed(Long couponId, Long userId) {
        return couponRedemptionRepository.countByCouponIdAndUserIdAndReversedFalse(couponId, userId) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public long remainingUserAllowance(Long couponId, Long userId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found: " + couponId));
        Integer limit = coupon.getUsagePerCustomer();
        if (limit == null) {
            return Long.MAX_VALUE;
        }
        long used = couponRedemptionRepository.countByCouponIdAndUserIdAndReversedFalse(couponId, userId);
        return Math.max(0, limit - used);
    }
}