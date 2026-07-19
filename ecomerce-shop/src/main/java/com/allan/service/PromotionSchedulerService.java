package com.allan.service;

/**
 * Activates and expires promotions (and synced {@code FlashSale} windows)
 * by date. Driven by a scheduled job, not a user-facing endpoint.
 *
 * <p><strong>Security architecture:</strong>
 * <ul>
 *   <li><strong>System-only.</strong> These methods must be invoked only
 *       by an internal scheduler ({@code @Scheduled} cron job or job
 *       runner), never exposed as a public/authenticated-user REST
 *       endpoint. If an HTTP trigger is ever needed for manual ops
 *       (re-running a missed tick), it must require a distinct internal
 *       service-role credential — never a regular seller/customer JWT —
 *       since forcing early activation or expiry directly manipulates
 *       checkout economics (e.g. prematurely ending a competitor's flash
 *       sale, or activating a not-yet-approved promotion).</li>
 *   <li><strong>DRAFT is never auto-activated.</strong>
 *       {@link #activateScheduledPromotions()} must only transition
 *       promotions that are already in an explicitly-approved,
 *       ready-to-go state (e.g. a scheduled/approved status distinct from
 *       DRAFT) — never a bare DRAFT, even if its {@code startsAt} has
 *       arrived. DRAFT means "still being edited/unapproved"; if the
 *       scheduler could promote DRAFT → ACTIVE automatically, an
 *       incomplete or unreviewed promotion could go live unattended.</li>
 *   <li><strong>Every transition is audited.</strong> Both methods must
 *       write a {@code PromotionAudit} entry via
 *       {@code PromotionAuditService.recordSystemAction(...)} with actor
 *       {@code "SCHEDULER"}, per {@code PromotionAudit}'s canonical action
 *       naming ({@code STATUS_CHANGED}).</li>
 *   <li><strong>Idempotent by design.</strong> Both methods must be safe
 *       to run repeatedly / concurrently (e.g. after a missed tick or a
 *       retry) without double-processing a promotion — implementations
 *       should re-check current status before transitioning rather than
 *       assuming a promotion found by the date query is still eligible.</li>
 * </ul>
 */
public interface PromotionSchedulerService {

    /** Flips ACTIVE promotions whose {@code endsAt} has passed to EXPIRED. */
    void expireEndedPromotions();

    /**
     * Activates promotions explicitly approved and scheduled whose
     * {@code startsAt} has arrived. Never touches plain DRAFT promotions —
     * see class javadoc.
     */
    void activateScheduledPromotions();

    /**
     * Keeps a {@code FlashSale}'s active/inactive state in sync with its
     * linked {@code Promotion}'s window, per the FlashSale-integration
     * note on {@code Promotion}.
     */
    void syncFlashSaleWindow(Long flashSaleId);
}