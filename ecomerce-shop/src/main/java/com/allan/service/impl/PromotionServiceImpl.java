package com.allan.service.impl;

import com.allan.domain.PromotionScope;
import com.allan.domain.PromotionStatus;
import com.allan.dto.PromotionCreateRequest;
import com.allan.exceptions.PromotionImmutableException;
import com.allan.exceptions.PromotionNotFoundException;
import com.allan.model.Promotion;
import com.allan.model.PromotionReward;
import com.allan.model.PromotionRule;
import com.allan.repository.CouponRedemptionRepository;
import com.allan.repository.PromotionRepository;
import com.allan.repository.PromotionRewardRepository;
import com.allan.repository.PromotionRuleRepository;
import com.allan.service.PromotionAuditService;
import com.allan.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * See {@link PromotionService} for the full security contract. Key points
 * enforced in this implementation specifically:
 * <ul>
 *   <li>Every seller-facing lookup goes through
 *       {@code PromotionRepository.findByIdAndSellerId}/{@code existsByIdAndSellerId}
 *       — never plain {@code findById} — so ownership is baked into the
 *       query, not an afterthought {@code if} check.</li>
 *   <li>Priority bands (1–10 admin / 50–200 seller) are validated on every
 *       write that touches priority, closing off the privilege-escalation
 *       path described in the interface javadoc.</li>
 *   <li>{@code @PreAuthorize} annotations are defense-in-depth on top of
 *       whatever controller-level checks exist — assumes method security is
 *       enabled ({@code @EnableMethodSecurity}) and roles named
 *       {@code ADMIN}/{@code SELLER} exist; adjust to your actual role
 *       naming if different.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private static final int ADMIN_PRIORITY_MIN = 1;
    private static final int ADMIN_PRIORITY_MAX = 10;
    private static final int SELLER_PRIORITY_MIN = 50;
    private static final int SELLER_PRIORITY_MAX = 200;

    /**
     * ASSUMPTION: PromotionStatus is {DRAFT, ACTIVE, PAUSED, EXPIRED} — no
     * separate ARCHIVED value. EXPIRED is therefore used as the one
     * terminal/retired state for BOTH "endsAt passed naturally" (via
     * PromotionSchedulerServiceImpl) AND "an admin manually retired this
     * promotion" (via updateStatus). If your enum actually has a distinct
     * value for either case, add it here and split the two meanings apart.
     */
    private static final Map<PromotionStatus, Set<PromotionStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(PromotionStatus.class);
    static {
        ALLOWED_TRANSITIONS.put(PromotionStatus.DRAFT, EnumSet.of(PromotionStatus.ACTIVE, PromotionStatus.EXPIRED));
        ALLOWED_TRANSITIONS.put(PromotionStatus.ACTIVE, EnumSet.of(PromotionStatus.PAUSED, PromotionStatus.EXPIRED));
        ALLOWED_TRANSITIONS.put(PromotionStatus.PAUSED, EnumSet.of(PromotionStatus.ACTIVE, PromotionStatus.EXPIRED));
        ALLOWED_TRANSITIONS.put(PromotionStatus.EXPIRED, EnumSet.noneOf(PromotionStatus.class));
    }

    private final PromotionRepository promotionRepository;
    private final PromotionRuleRepository promotionRuleRepository;
    private final PromotionRewardRepository promotionRewardRepository;
    private final CouponRedemptionRepository couponRedemptionRepository;
    private final PromotionAuditService auditService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Promotion createPlatformPromotion(PromotionCreateRequest request) {
        if (request.scope() == null) {
            throw new IllegalArgumentException("scope is required");
        }
        int priority = request.priority() != null ? request.priority() : ADMIN_PRIORITY_MAX;
        validatePriorityBand(priority, true);

        Promotion promotion = buildPromotion(request, null, priority);
        promotion = promotionRepository.save(promotion);
        auditService.record(promotion.getId(), "PROMOTION_CREATED", null, promotion.getName(), null);
        return promotion;
    }

    @Override
    @Transactional
    public Promotion createSellerPromotion(Long sellerId, PromotionCreateRequest request) {
        if (sellerId == null) {
            throw new IllegalArgumentException("sellerId is required");
        }
        if (request.scope() == PromotionScope.PLATFORM_WIDE) {
            // Defense in depth: a seller must never be able to create a
            // platform-wide promotion even if a controller-level check is
            // missing or misconfigured upstream.
            throw new IllegalArgumentException("Sellers cannot create PLATFORM_WIDE promotions");
        }
        int priority = request.priority() != null ? request.priority() : SELLER_PRIORITY_MAX / 2;
        validatePriorityBand(priority, false);

        Promotion promotion = buildPromotion(request, sellerId, priority);
        promotion = promotionRepository.save(promotion);
        auditService.record(promotion.getId(), "PROMOTION_CREATED", null, promotion.getName(), null);
        return promotion;
    }

    @Override
    @Transactional(readOnly = true)
    public Promotion getForSeller(Long promotionId, Long sellerId) {
        return promotionRepository.findByIdAndSellerId(promotionId, sellerId)
                .orElseThrow(() -> new PromotionNotFoundException(promotionId));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Promotion getForAdmin(Long promotionId) {
        return promotionRepository.findById(promotionId)
                .orElseThrow(() -> new PromotionNotFoundException(promotionId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Promotion> listForSeller(Long sellerId, Pageable pageable) {
        return promotionRepository.findBySellerId(sellerId, pageable);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Page<Promotion> listPlatformPromotions(Pageable pageable) {
        return promotionRepository.findPlatformWide(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Promotion> listByStatus(PromotionStatus status, Pageable pageable) {
        return promotionRepository.findByStatus(status, pageable);
    }

    @Override
    @Transactional
    public Promotion updateStatus(Long promotionId, Long sellerId, PromotionStatus newStatus) {
        Promotion promotion = resolveForWrite(promotionId, sellerId);
        PromotionStatus oldStatus = promotion.getStatus();

        Set<PromotionStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(oldStatus, EnumSet.noneOf(PromotionStatus.class));
        if (!allowed.contains(newStatus)) {
            throw new IllegalArgumentException(
                    "Cannot transition promotion " + promotionId + " from " + oldStatus + " to " + newStatus);
        }

        promotion.setStatus(newStatus);
        promotion = promotionRepository.save(promotion);
        auditService.record(promotionId, "STATUS_CHANGED", oldStatus.name(), newStatus.name(), null);
        return promotion;
    }

    @Override
    @Transactional
    public Promotion updateSchedule(Long promotionId, Long sellerId, LocalDateTime newEndsAt) {
        Promotion promotion = resolveForWrite(promotionId, sellerId);
        if (newEndsAt == null || !newEndsAt.isAfter(promotion.getStartsAt())) {
            throw new IllegalArgumentException("endsAt must be after startsAt");
        }
        String old = promotion.getEndsAt().toString();
        promotion.setEndsAt(newEndsAt);
        promotion = promotionRepository.save(promotion);
        auditService.record(promotionId, "SCHEDULE_UPDATED", old, newEndsAt.toString(), null);
        return promotion;
    }

    @Override
    @Transactional
    public Promotion updatePriority(Long promotionId, Long sellerId, int newPriority) {
        Promotion promotion = resolveForWrite(promotionId, sellerId);
        // Band is determined by the promotion's OWN ownership type, not by
        // who's calling — a platform promotion always stays in 1–10 even
        // if somehow edited through an admin-as-seller path.
        validatePriorityBand(newPriority, promotion.getSellerId() == null);

        int old = promotion.getPriority();
        promotion.setPriority(newPriority);
        promotion = promotionRepository.save(promotion);
        auditService.record(promotionId, "PRIORITY_CHANGED", String.valueOf(old), String.valueOf(newPriority), null);
        return promotion;
    }

    @Override
    @Transactional
    public PromotionRule addRule(Long promotionId, Long sellerId, PromotionRule rule) {
        Promotion promotion = resolveForWrite(promotionId, sellerId);
        assertMutable(promotion);

        rule.setPromotion(promotion);
        PromotionRule saved = promotionRuleRepository.save(rule);
        auditService.record(promotionId, "RULE_ADDED", null, describeRule(saved), null);
        return saved;
    }

    @Override
    @Transactional
    public void removeRule(Long promotionId, Long sellerId, Long ruleId) {
        Promotion promotion = resolveForWrite(promotionId, sellerId);
        assertMutable(promotion);

        PromotionRule rule = promotionRuleRepository.findById(ruleId)
                .filter(r -> r.getPromotion().getId().equals(promotionId))
                .orElseThrow(() -> new IllegalArgumentException("Rule " + ruleId + " does not belong to promotion " + promotionId));

        promotionRuleRepository.delete(rule);
        auditService.record(promotionId, "RULE_REMOVED", describeRule(rule), null, null);
    }

    @Override
    @Transactional
    public PromotionReward addReward(Long promotionId, Long sellerId, PromotionReward reward) {
        Promotion promotion = resolveForWrite(promotionId, sellerId);
        assertMutable(promotion);

        // A seller-owned promotion may only grant rewards scoped to their
        // OWN catalog. Cross-vendor reward assignment on a seller's own
        // promotion would let seller A make the platform (or seller B)
        // foot the bill for a discount seller A configured.
        if (promotion.getSellerId() != null
                && reward.getApplicableSellerId() != null
                && !reward.getApplicableSellerId().equals(promotion.getSellerId())) {
            throw new IllegalArgumentException(
                    "A seller-owned promotion cannot grant rewards scoped to a different seller");
        }

        reward.setPromotion(promotion);
        PromotionReward saved = promotionRewardRepository.save(reward);
        auditService.record(promotionId, "REWARD_UPDATED", null, describeReward(saved), null);
        return saved;
    }

    @Override
    @Transactional
    public void removeReward(Long promotionId, Long sellerId, Long rewardId) {
        Promotion promotion = resolveForWrite(promotionId, sellerId);
        assertMutable(promotion);

        PromotionReward reward = promotionRewardRepository.findById(rewardId)
                .filter(r -> r.getPromotion().getId().equals(promotionId))
                .orElseThrow(() -> new IllegalArgumentException("Reward " + rewardId + " does not belong to promotion " + promotionId));

        promotionRewardRepository.delete(reward);
        auditService.record(promotionId, "REWARD_UPDATED", describeReward(reward), null, "removed");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasConfirmedRedemptions(Long promotionId) {
        return couponRedemptionRepository.existsByCoupon_Promotion_IdAndReversedFalse(promotionId);
    }

    // ── internal helpers ─────────────────────────────────────────────────

    /**
     * Resolves a promotion for a mutating operation. {@code sellerId == null}
     * signals an admin-privileged call (already gated by controller-level
     * {@code @PreAuthorize}/method security upstream) and bypasses the
     * ownership filter; any non-null {@code sellerId} always goes through
     * the ownership-scoped query.
     */
    private Promotion resolveForWrite(Long promotionId, Long sellerId) {
        if (sellerId == null) {
            return promotionRepository.findById(promotionId)
                    .orElseThrow(() -> new PromotionNotFoundException(promotionId));
        }
        return promotionRepository.findByIdAndSellerId(promotionId, sellerId)
                .orElseThrow(() -> new PromotionNotFoundException(promotionId));
    }

    private void assertMutable(Promotion promotion) {
        if (hasConfirmedRedemptions(promotion.getId())) {
            throw new PromotionImmutableException(promotion.getId());
        }
    }

    private void validatePriorityBand(int priority, boolean isPlatform) {
        int min = isPlatform ? ADMIN_PRIORITY_MIN : SELLER_PRIORITY_MIN;
        int max = isPlatform ? ADMIN_PRIORITY_MAX : SELLER_PRIORITY_MAX;
        if (priority < min || priority > max) {
            throw new IllegalArgumentException(
                    (isPlatform ? "Platform" : "Seller") + " promotion priority must be between " + min + " and " + max);
        }
    }

    private Promotion buildPromotion(PromotionCreateRequest request, Long sellerId, int priority) {
        Promotion promotion = new Promotion();
        promotion.setSellerId(sellerId);
        promotion.setName(request.name());
        promotion.setDescription(request.description());
        promotion.setType(request.type());
        promotion.setScope(request.scope());
        promotion.setStatus(PromotionStatus.DRAFT);
        promotion.setPriority(priority);
        promotion.setStackable(request.stackable());
        promotion.setExclusive(request.exclusive());
        promotion.setMinimumOrderValue(request.minimumOrderValue());
        promotion.setMaximumDiscountAmount(request.maximumDiscountAmount());
        promotion.setStartsAt(request.startsAt());
        promotion.setEndsAt(request.endsAt());
        return promotion;
    }

    private String describeRule(PromotionRule rule) {
        return rule.getRuleType() + " " + rule.getOperator() + " " + rule.getValue();
    }

    private String describeReward(PromotionReward reward) {
        return reward.getRewardType() + "=" + reward.getValue();
    }
}