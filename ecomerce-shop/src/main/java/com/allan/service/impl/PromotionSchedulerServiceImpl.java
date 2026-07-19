package com.allan.service.impl;

import com.allan.domain.PromotionStatus;
import com.allan.model.Promotion;
import com.allan.repository.PromotionRepository;
import com.allan.service.PromotionAuditService;
import com.allan.service.PromotionSchedulerService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * See {@link PromotionSchedulerService} for the full security contract. Key
 * points enforced here specifically:
 * <ul>
 *   <li><strong>No REST-reachable entry point.</strong> These methods are
 *       invoked only via {@code @Scheduled} cron triggers (requires
 *       {@code @EnableScheduling} on a config class elsewhere). There is
 *       deliberately no controller in this module that calls into this
 *       bean — if manual/on-demand triggering is ever needed, expose it
 *       behind a distinct internal-service-role endpoint, never a normal
 *       seller/customer JWT.</li>
 *   <li><strong>{@code activateScheduledPromotions()} is currently a no-op.</strong>
 *       {@code PromotionStatus} has no status distinct from {@code DRAFT}
 *       for "approved but not yet live," so there's no safe candidate set
 *       to auto-activate — see the method body comment for how to enable
 *       this properly if/when such a status is added.</li>
 *   <li>Every transition re-checks the promotion's current status inside
 *       the loop before writing, so a concurrent manual status change (or
 *       a second scheduler tick firing early) can't cause a double
 *       transition or an audit entry for a no-op change.</li>
 *   <li>{@code FlashSale} sync is stubbed — the entity wasn't available to
 *       this module, so wire in a real {@code FlashSaleRepository} lookup
 *       before relying on this in production.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class PromotionSchedulerServiceImpl implements PromotionSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(PromotionSchedulerServiceImpl.class);
    private static final String SYSTEM_ACTOR = "SCHEDULER";

    private final PromotionRepository promotionRepository;
    private final PromotionAuditService auditService;

    @Override
    @Scheduled(fixedDelayString = "${promotion.scheduler.expire-interval-ms:60000}")
    @Transactional
    public void expireEndedPromotions() {
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> actives = promotionRepository.findByStatus(PromotionStatus.ACTIVE);

        for (Promotion promotion : actives) {
            // Re-check status defensively — another thread/tick may have
            // already transitioned this promotion since the query ran.
            if (promotion.getStatus() != PromotionStatus.ACTIVE) {
                continue;
            }
            if (now.isAfter(promotion.getEndsAt())) {
                promotion.setStatus(PromotionStatus.EXPIRED);
                promotionRepository.save(promotion);
                auditService.recordSystemAction(promotion.getId(), "STATUS_CHANGED",
                        PromotionStatus.ACTIVE.name(), PromotionStatus.EXPIRED.name(),
                        "Auto-expired: endsAt passed", SYSTEM_ACTOR);
            }
        }
    }

    @Override
    @Scheduled(fixedDelayString = "${promotion.scheduler.activate-interval-ms:60000}")
    @Transactional
    public void activateScheduledPromotions() {
        // CORRECTION: PromotionStatus does not have a SCHEDULED value (or
        // any status distinct from DRAFT for an "approved, not yet live"
        // promotion). Without that distinction, this method has no safe
        // implementation: the only candidate pool would be DRAFT, and
        // auto-activating DRAFT is the one thing this scheduler must never
        // do (see interface javadoc — DRAFT means "still being edited/
        // unapproved", so silently promoting it to ACTIVE just because
        // startsAt arrived could put an incomplete or unreviewed promotion
        // live unattended).
        //
        // This is intentionally a no-op until one of the following exists:
        //   1. Add a SCHEDULED (or similarly named) status to
        //      PromotionStatus for admin/seller-approved-but-pending
        //      promotions, then restore the original query/transition
        //      logic against that value, or
        //   2. Skip automatic activation entirely and have
        //      PromotionController/VendorPromotionController's existing
        //      updateStatus(..., ACTIVE) be the only activation path
        //      (i.e. someone manually flips DRAFT -> ACTIVE when ready,
        //      rather than the scheduler doing it by date).
        log.debug("activateScheduledPromotions() is a no-op: PromotionStatus has no SCHEDULED-equivalent value. "
                + "See method comment for how to enable real auto-activation.");
    }

    @Override
    @Transactional
    public void syncFlashSaleWindow(Long flashSaleId) {
        // TODO: wire in FlashSaleRepository once available — this module
        // was given Promotion's javadoc description of the FlashSale
        // integration but not the FlashSale entity itself, so the actual
        // activate/deactivate-in-sync logic can't be implemented here yet.
        log.warn("syncFlashSaleWindow({}) called but FlashSale integration is not yet wired in", flashSaleId);
    }
}