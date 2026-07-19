package com.allan.listener;

import com.allan.cache.PromotionCacheService;
import com.allan.event.PromotionChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Reacts to a committed promotion mutation by invalidating
 * {@link PromotionCacheService}'s active-promotions cache.
 *
 * <p><strong>Does not write {@code PromotionAudit}.</strong> That write
 * already happens synchronously inside {@code PromotionServiceImpl}, in the
 * same transaction as the mutation — see {@code PromotionAudit}'s own
 * javadoc ("Written by PromotionServiceImpl on every meaningful mutation").
 * Writing it again here would duplicate the audit trail. This listener's
 * only responsibility is the async cache side effect.
 *
 * <p>{@code AFTER_COMMIT} matters here too: evicting the cache before the
 * transaction commits could let a concurrent read repopulate the cache with
 * the stale pre-change data a moment before the commit lands, leaving the
 * cache wrong until the next change or TTL expiry.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PromotionChangedEventListener {

    private final PromotionCacheService promotionCacheService;

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPromotionChanged(PromotionChangedEvent event) {
        log.info("Invalidating active-promotions cache: promotionId={}, changeType={}",
                event.promotionId(), event.changeType());
        promotionCacheService.evictActivePromotions();
    }
}