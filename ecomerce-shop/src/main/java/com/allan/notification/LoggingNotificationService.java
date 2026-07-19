package com.allan.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Temporary {@link NotificationService} implementation that only logs.
 *
 * <p><strong>Stub — replace with a real integration</strong> (e.g. a
 * {@code JavaMailSender}-backed {@code EmailNotificationService}) once one
 * exists. Nothing else in the codebase needs to change when you do — every
 * caller depends on {@link NotificationService}, not this class.
 */
@Slf4j
@Service
public class LoggingNotificationService implements NotificationService {

    @Override
    public void sendCouponRedeemedConfirmation(Long userId, Long orderId, long discountApplied) {
        log.info("[STUB] Would send redemption confirmation — user={}, order={}, discountApplied=UGX {}",
                userId, orderId, discountApplied);
    }
}