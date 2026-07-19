package com.allan.service;

import com.allan.model.PromotionAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * <strong>Not in the original file list — added because every other
 * service in this package (Promotion, Coupon, Redemption, Scheduler)
 * needs to write {@code PromotionAudit} entries, and without one shared
 * service each of them would hand-roll audit writes independently. That's
 * exactly how enterprise audit trails end up with gaps: one service
 * forgets a call, another sets the actor field slightly differently, and
 * the log stops being trustworthy for compliance/dispute resolution.</strong>
 *
 * <p><strong>Security architecture:</strong>
 * <ul>
 *   <li><strong>Actor is never a parameter for human actions.</strong>
 *       {@link #record} resolves the acting user from
 *       {@code SecurityContextHolder} internally — it deliberately does
 *       NOT accept an {@code actor} argument for human-triggered changes.
 *       If it did, any caller could pass an arbitrary string and forge
 *       the audit trail (e.g. attribute their own change to another
 *       admin). Only {@link #recordSystemAction} accepts an actor, and
 *       only from a small fixed set of system constants
 *       ({@code "SCHEDULER"}, {@code "REDEMPTION_SERVICE"}) — never
 *       free text.</li>
 *   <li><strong>Append-only, matching {@code PromotionAuditRepository}.</strong>
 *       This interface exposes no update/delete method, mirroring the
 *       repository's deliberately restricted surface.</li>
 *   <li><strong>Read access is ownership-scoped.</strong>
 *       {@link #findHistory} takes {@code requestingSellerId} (nullable
 *       for admin callers) so a seller can only view audit history for
 *       promotions they own; audit entries can reveal business-sensitive
 *       detail (reward values, rule thresholds) that must not leak across
 *       tenants.</li>
 *   <li>Uses {@code PromotionAudit}'s static factories
 *       ({@code ofCreation}, {@code ofStatusChange}, etc.) internally
 *       rather than constructing entries ad hoc, per that class's javadoc.</li>
 * </ul>
 */
public interface PromotionAuditService {

    /**
     * Writes an audit entry for a human-triggered change. Actor is
     * resolved internally from the security context — see class javadoc.
     */
    void record(Long promotionId, String action, String oldValue, String newValue, String notes);

    /**
     * Writes an audit entry for a system-triggered change (scheduler,
     * redemption reversal). {@code systemActor} must be one of a small
     * fixed set of constants, never arbitrary input.
     */
    void recordSystemAction(Long promotionId, String action, String oldValue, String newValue,
                             String notes, String systemActor);

    /**
     * @param requestingSellerId if non-null, results are filtered to
     *        promotions owned by this seller; pass {@code null} only for
     *        admin callers.
     */
    Page<PromotionAudit> findHistory(Long promotionId, Long requestingSellerId, Pageable pageable);
}