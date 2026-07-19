package com.allan.controller;

import com.allan.dto.CouponCreateRequest;
import com.allan.response.CouponResponse;
import com.allan.mapper.PromotionMapper;
import com.allan.model.Coupon;
import com.allan.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin-only coupon administration.
 *
 * <p><strong>Refactored from the original {@code AdminCouponController}.
 * What changed, and why:</strong>
 * <ul>
 *   <li><strong>No more {@code @RequestBody Coupon coupon}.</strong> The
 *       original bound the raw JPA entity directly to client JSON, which
 *       means a caller could set {@code id}, {@code usedCount},
 *       {@code status}, or {@code version} themselves — a mass-assignment
 *       vulnerability, not a style nitpick. Every write here now goes
 *       through {@link CouponCreateRequest}, which only exposes the fields
 *       a client is legitimately allowed to set.</li>
 *   <li><strong>{@code applyCoupon}/{@code removeCoupon} are gone.</strong>
 *       Those are customer-facing operations against a specific customer's
 *       cart/order — they don't belong on an admin controller, and they're
 *       already implemented correctly (server-rebuilt {@code CartContext},
 *       no client-supplied {@code double orderValue}) on
 *       {@code CheckoutCouponController}. Mixing admin and customer
 *       concerns in one controller made it easy to accidentally apply
 *       admin-level trust to a customer-supplied value, or vice versa.</li>
 *   <li><strong>No manual JWT parsing.</strong> The original called
 *       {@code userService.findUserByJwtToken(jwt)} on every request. Once
 *       Spring Security's filter chain populates the
 *       {@code SecurityContext}, {@code @PreAuthorize} on this class
 *       handles authorization and there's no need to touch the token here
 *       at all — this controller doesn't even need to know who the admin
 *       is beyond what {@code @PreAuthorize} already checked (audit
 *       attribution happens inside {@code PromotionAuditServiceImpl} via
 *       {@code SecurityContextHolder}, not by passing identity through
 *       every layer manually).</li>
 *   <li><strong>{@code deleteCoupon} → {@code disableCoupon}.</strong> No
 *       hard delete: coupons with redemption history must stay queryable
 *       for the audit trail and for {@code RedemptionService.reverse(...)}
 *       to keep working correctly. Deactivation is a status change, not a
 *       row removal — consistent with the append-only philosophy the rest
 *       of this module follows.</li>
 *   <li><strong>Responses use {@link CouponResponse}, never the raw entity.</strong>
 *       Same reasoning as inbound binding, in reverse: returning
 *       {@code Coupon} directly would serialize {@code version} and
 *       whatever else gets added to the entity later, by default.</li>
 *   <li><strong>Everything paginated.</strong> The original's
 *       {@code /admin/all} returned every coupon on the platform in one
 *       response — fine at 50 coupons, a real problem once a seller has
 *       bulk-generated a few thousand.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/admin/coupons")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponService couponService;
    private final PromotionMapper mapper;

    @PostMapping("/promotions/{promotionId}/coupons")
    public ResponseEntity<CouponResponse> createCoupon(@PathVariable Long promotionId,
                                                         @Valid @RequestBody CouponCreateRequest request) {
        Coupon created = couponService.createAdminCoupon(promotionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    @PostMapping("/{id}/disable")
    public ResponseEntity<Void> disableCoupon(@PathVariable Long id,
                                               @RequestParam(required = false) String reason) {
        couponService.disableCouponAsAdmin(id, reason);
        return ResponseEntity.noContent().build();
    }

    /** Platform-wide, paginated. Omit {@code promotionId} to list across all promotions. */
    @GetMapping
    public Page<CouponResponse> list(@RequestParam(required = false) Long promotionId, Pageable pageable) {
        Page<Coupon> page = promotionId != null
                ? couponService.findByPromotionIdAsAdmin(promotionId, pageable)
                : couponService.findAllForAdmin(pageable);
        return page.map(mapper::toResponse);
    }
}