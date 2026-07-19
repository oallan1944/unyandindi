package com.allan.service.impl;

import com.allan.exceptions.PromotionNotFoundException;
import com.allan.model.Promotion;
import com.allan.model.PromotionAudit;
import com.allan.repository.PromotionAuditRepository;
import com.allan.repository.PromotionRepository;
import com.allan.service.PromotionAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class PromotionAuditServiceImpl implements PromotionAuditService {

    /**
     * The only actor strings ever accepted for system-triggered entries.
     * Keeping this as a closed set (rather than accepting any string) means
     * a bug elsewhere in the codebase can't accidentally write an
     * attacker-controlled value into the audit trail's actor column.
     */
    private static final Set<String> ALLOWED_SYSTEM_ACTORS = Set.of(
            "SCHEDULER", "REDEMPTION_SERVICE"
    );

    private final PromotionAuditRepository auditRepository;
    private final PromotionRepository promotionRepository;

    @Override
    @Transactional
    public void record(Long promotionId, String action, String oldValue, String newValue, String notes) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new PromotionNotFoundException(promotionId));

        // Actor is ALWAYS resolved server-side from the security context —
        // never accepted as a method parameter for human-triggered changes.
        // See class/interface javadoc for why.
        String actor = resolveCurrentActor();

        PromotionAudit entry = PromotionAudit.of(promotion, action, oldValue, newValue, actor, notes);
        auditRepository.save(entry);
    }

    @Override
    @Transactional
    public void recordSystemAction(Long promotionId, String action, String oldValue, String newValue,
                                    String notes, String systemActor) {
        if (!ALLOWED_SYSTEM_ACTORS.contains(systemActor)) {
            throw new IllegalArgumentException(
                    "Unrecognized system actor '" + systemActor + "' — must be one of " + ALLOWED_SYSTEM_ACTORS);
        }
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new PromotionNotFoundException(promotionId));

        PromotionAudit entry = PromotionAudit.of(promotion, action, oldValue, newValue, systemActor, notes);
        auditRepository.save(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PromotionAudit> findHistory(Long promotionId, Long requestingSellerId, Pageable pageable) {
        if (requestingSellerId != null
                && !promotionRepository.existsByIdAndSellerId(promotionId, requestingSellerId)) {
            // Same collapsing of "missing" and "not yours" as everywhere
            // else — a seller must not be able to tell the two apart.
            throw new PromotionNotFoundException(promotionId);
        }
        return auditRepository.findByPromotionIdOrderByCreatedAtDesc(promotionId, pageable);
    }

    private String resolveCurrentActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            // Fail loud rather than silently writing "anonymous"/"null" —
            // an unauthenticated write path reaching this point is itself
            // a bug worth surfacing immediately.
            throw new IllegalStateException("Cannot record a human-actor audit entry without an authenticated principal");
        }
        return auth.getName();
    }
}