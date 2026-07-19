package com.allan.service;

import com.allan.dto.CartContext;
import com.allan.dto.CouponCreateRequest;
import com.allan.dto.PromotionEvaluationResult;
import com.allan.exceptions.CouponNotFoundException;
import com.allan.exceptions.CouponRedemptionException;
import com.allan.model.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Coupon issuance, lookup, and pre-checkout validation.
 *
 * <p><strong>Security architecture:</strong>
 * <ul>
 *   <li><strong>Code normalization is centralized here.</strong> Per
 *       {@code Coupon}'s javadoc, codes are always stored/compared upper
 *       case. Every method that accepts a raw code MUST call
 *       {@link #normalizeCode(String)} internally before touching the
 *       repository — controllers and other services must never normalize
 *       independently, since divergent normalization logic is how
 *       case-variant duplicate codes slip past uniqueness checks.</li>
 *   <li><strong>Ownership-scoped issuance.</strong> {@code createCoupon}
 *       and {@code bulkGenerateCoupons} take {@code sellerId} and MUST
 *       verify it against the parent promotion's owner before creating
 *       anything — otherwise a seller could attach coupons to a
 *       platform-wide or competitor's promotion.</li>
 *   <li><strong>Unguessable codes.</strong> Bulk-generated codes MUST use a
 *       cryptographically secure random source (e.g.
 *       {@code UUID.randomUUID()} or {@code SecureRandom}), never a
 *       predictable sequence (SAVE-0001, SAVE-0002, ...) — sequential codes
 *       let an attacker enumerate valid codes by incrementing a counter.</li>
 *   <li><strong>{@code validate} is read-only.</strong> It must never
 *       increment {@code usedCount} or write a redemption — that's
 *       {@code RedemptionService}'s job. Keeping validation side-effect-free
 *       is what makes it safe to call repeatedly for live cart previews
 *       without corrupting usage counters, and safe to call again
 *       internally by {@code RedemptionService} immediately before the
 *       actual redemption (defense against cart/coupon state changing
 *       between preview and checkout).</li>
 *   <li>Rate limiting repeated {@code validate}/{@code findByCode} calls
 *       against guessed codes is expected to be enforced at the API
 *       gateway/controller layer (e.g. per-IP or per-user throttling) —
 *       this service does not implement throttling itself.</li>
 * </ul>
 */
public interface CouponService {

    /**
     * @throws com.allan.exception.PromotionNotFoundException if the promotion
     *         doesn't exist or isn't owned by {@code sellerId}.
     */
    Coupon createCoupon(Long promotionId, Long sellerId, CouponCreateRequest request);

    /** Admin-only; promotion may be platform-wide or any seller's. */
    Coupon createAdminCoupon(Long promotionId, CouponCreateRequest request);

    /**
     * Generates {@code count} coupons under one promotion using
     * cryptographically random codes, ignoring any {@code code} field on
     * the template.
     */
    List<Coupon> bulkGenerateCoupons(Long promotionId, Long sellerId, int count, CouponCreateRequest template);

    /** @throws CouponNotFoundException if missing or not owned (via parent promotion) by {@code sellerId}. */
    void disableCoupon(Long couponId, Long sellerId, String reason);

    /** Admin-only. No ownership filter — may disable any coupon on the platform. */
    void disableCouponAsAdmin(Long couponId, String reason);

    /** Normalizes {@code code} before lookup. Safe to expose to any authenticated caller — read-only. */
    Optional<Coupon> findByCode(String code);

    Page<Coupon> findByPromotionId(Long promotionId, Long sellerId, Pageable pageable);

    /** Admin-only. No ownership filter. */
    Page<Coupon> findByPromotionIdAsAdmin(Long promotionId, Pageable pageable);

    /**
     * Admin-only, platform-wide listing across every seller's coupons.
     * Always paginated — a bulk-generated promotion can carry thousands of
     * coupons, so an unpaginated "list everything" is a resource-exhaustion
     * risk, not just a UX inconvenience.
     */
    Page<Coupon> findAllForAdmin(Pageable pageable);

    /**
     * Full pre-checkout validation: normalizes the code, checks
     * {@code Coupon.isAvailable()}, the promotion's live window, the
     * caller's per-user remaining allowance, and cart rule eligibility.
     * Read-only — see class javadoc.
     *
     * @throws CouponRedemptionException with a specific {@link CouponRedemptionException.Reason}
     *         describing why the code can't be used right now.
     */
    PromotionEvaluationResult validate(String code, CartContext cart);

    /** Upper-cases and trims; the single source of truth for code normalization. */
    String normalizeCode(String rawCode);
}