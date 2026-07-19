package com.allan.listener;

import com.allan.event.CouponRedeemedEvent;
import com.allan.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Reacts to a successfully committed coupon redemption.
 *
 * <p><strong>{@code AFTER_COMMIT} is mandatory, not a style choice.</strong>
 * {@code RedemptionServiceImpl} publishes {@link CouponRedeemedEvent} inside
 * the same transaction that writes the {@code CouponRedemption} row. If this
 * listener ran on a plain {@code @EventListener} instead, it could fire
 * before that transaction commits — or worse, after it rolls back — sending
 * a confirmation for a redemption that never actually happened.
 * {@code @TransactionalEventListener(AFTER_COMMIT)} guarantees the write is
 * durable before this runs.
 *
 * <p>{@code @Async} keeps notification latency off the checkout request
 * path — the customer gets their redemption result immediately; the
 * confirmation follows on the {@code taskExecutor} pool.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CouponRedeemedEventListener {

    private final NotificationService notificationService;

    // TODO: inject a usage-stats repository/service once that table/entity
    // exists. Nothing currently in the model list (Coupon.usedCount is
    // updated synchronously by RedemptionServiceImpl itself) needs this —
    // only add it if a separate analytics/reporting aggregate is introduced.

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCouponRedeemed(CouponRedeemedEvent event) {
        log.info("Processing CouponRedeemedEvent: redemptionId={}, couponId={}, orderId={}",
                event.redemptionId(), event.couponId(), event.orderId());
        try {
            notificationService.sendCouponRedeemedConfirmation(
                    event.userId(), event.orderId(), event.discountApplied());
        } catch (Exception ex) {
            // Notification failure must never surface as a redemption failure —
            // the redemption already committed. Log and move on; a delivery
            // failure here is a notification-system concern, not a checkout one.
            log.error("Failed to send redemption confirmation for order {}", event.orderId(), ex);
        }
    }
}