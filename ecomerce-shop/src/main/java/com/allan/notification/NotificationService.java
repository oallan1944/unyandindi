package com.allan.notification;

/**
 * Abstraction over outbound customer notifications triggered by
 * checkout/promotion events. Kept separate from any general-purpose mailer
 * so the delivery channel (email today, SMS/push later) can change without
 * touching {@code CouponRedeemedEventListener}.
 */
public interface NotificationService {

    /**
     * @param discountApplied UGX whole shillings
     */
    void sendCouponRedeemedConfirmation(Long userId, Long orderId, long discountApplied);
}