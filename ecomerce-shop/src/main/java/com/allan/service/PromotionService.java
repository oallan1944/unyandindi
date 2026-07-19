package com.allan.service;

import com.allan.domain.PromotionStatus;
import com.allan.dto.PromotionCreateRequest;
import com.allan.exceptions.PromotionImmutableException;
import com.allan.exceptions.PromotionNotFoundException;
import com.allan.model.Promotion;
import com.allan.model.PromotionReward;
import com.allan.model.PromotionRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Lifecycle management for {@link Promotion}: creation, scheduling, status
 * transitions, and rule/reward composition.
 *
 * <p><strong>Security architecture:</strong>
 * <ul>
 *   <li><strong>Ownership is a parameter, not a lookup.</strong> Every
 *       seller-facing method takes {@code sellerId} explicitly and the
 *       implementation MUST resolve the promotion via
 *       {@code PromotionRepository.findByIdAndSellerId(...)}, never plain
 *       {@code findById(...)} followed by an {@code if} check — this makes
 *       the ownership filter part of the query itself. {@code sellerId}
 *       must always come from the authenticated principal
 *       (SecurityContext), never from a request body.</li>
 *   <li><strong>Uniform not-found.</strong> "Doesn't exist" and "exists but
 *       belongs to someone else" both throw {@link PromotionNotFoundException}
 *       — see that class's javadoc for why they must not be distinguished.</li>
 *   <li><strong>Priority band enforcement.</strong> Admin platform
 *       promotions use priority 1–10; seller promotions use 50–200 (per
 *       {@code Promotion.priority} javadoc). {@code createSellerPromotion}
 *       and {@code updatePriority} MUST reject any seller-supplied value
 *       outside 50–200 — without this check, a seller could set
 *       priority = 1 and have their promotion silently outrank every
 *       platform-wide promotion at checkout, a direct
 *       privilege-escalation / revenue-integrity issue, not just a
 *       cosmetic ordering bug.</li>
 *   <li><strong>Scope enforcement.</strong> {@code createSellerPromotion}
 *       MUST reject {@code PromotionScope.PLATFORM_WIDE} — only
 *       {@code createPlatformPromotion} (admin-only, enforced at the
 *       controller/method-security layer) may create platform-wide
 *       promotions.</li>
 *   <li><strong>Immutability after redemptions.</strong>
 *       {@code addRule}/{@code removeRule}/{@code addReward}/{@code removeReward}
 *       throw {@link PromotionImmutableException} once the promotion has
 *       any confirmed {@code CouponRedemption}. {@code updateSchedule},
 *       {@code updateStatus}, and {@code updatePriority} remain allowed.</li>
 *   <li><strong>No hard delete.</strong> There is deliberately no
 *       {@code deletePromotion} method. Promotions are retired via
 *       {@code updateStatus(..., ARCHIVED)} so audit history and any
 *       historical redemptions remain queryable.</li>
 *   <li>Every mutation must be paired with a {@code PromotionAuditService}
 *       call by the implementation — this interface doesn't expose audit
 *       writes directly so callers can't bypass or spoof them.</li>
 * </ul>
 */
public interface PromotionService {

    /** Admin-only. Creates a platform-wide promotion ({@code sellerId == null}). */
    Promotion createPlatformPromotion(PromotionCreateRequest request);

    /**
     * Creates a promotion owned by {@code sellerId}. Rejects
     * {@code PromotionScope.PLATFORM_WIDE} and any priority outside 50–200.
     */
    Promotion createSellerPromotion(Long sellerId, PromotionCreateRequest request);

    /** @throws PromotionNotFoundException if missing or not owned by {@code sellerId}. */
    Promotion getForSeller(Long promotionId, Long sellerId);

    /** Admin-only; no ownership filter. */
    Promotion getForAdmin(Long promotionId);

    Page<Promotion> listForSeller(Long sellerId, Pageable pageable);

    /** Admin-only. Optionally filter to platform-wide rows only. */
    Page<Promotion> listPlatformPromotions(Pageable pageable);

    Page<Promotion> listByStatus(PromotionStatus status, Pageable pageable);

    /**
     * Validates the transition (e.g. rejects ACTIVE → DRAFT) and writes an
     * audit entry. Always allowed regardless of redemption history.
     */
    Promotion updateStatus(Long promotionId, Long sellerId, PromotionStatus newStatus);

    /** Always allowed regardless of redemption history. */
    Promotion updateSchedule(Long promotionId, Long sellerId, java.time.LocalDateTime newEndsAt);

    /**
     * Always allowed regardless of redemption history, but bounded to the
     * caller's priority band (1–10 admin / 50–200 seller).
     */
    Promotion updatePriority(Long promotionId, Long sellerId, int newPriority);

    /** @throws PromotionImmutableException if confirmed redemptions exist. */
    PromotionRule addRule(Long promotionId, Long sellerId, PromotionRule rule);

    /** @throws PromotionImmutableException if confirmed redemptions exist. */
    void removeRule(Long promotionId, Long sellerId, Long ruleId);

    /**
     * @throws PromotionImmutableException if confirmed redemptions exist.
     * @throws IllegalArgumentException if {@code reward.getApplicableSellerId()}
     *         is set to a seller other than {@code sellerId} on a non
     *         platform-wide promotion — a seller may only grant rewards
     *         scoped to their own catalog, never another vendor's.
     */
    PromotionReward addReward(Long promotionId, Long sellerId, PromotionReward reward);

    /** @throws PromotionImmutableException if confirmed redemptions exist. */
    void removeReward(Long promotionId, Long sellerId, Long rewardId);

    boolean hasConfirmedRedemptions(Long promotionId);
}