package com.allan.controller;

import com.allan.domain.PromotionStatus;
import com.allan.dto.CouponCreateRequest;
import com.allan.response.CouponResponse;
import com.allan.dto.PromotionCreateRequest;
import com.allan.request.PromotionRewardRequest;
import com.allan.request.PromotionRuleRequest;
import com.allan.response.PromotionResponse;
import com.allan.response.PromotionRewardResponse;
import com.allan.response.PromotionRuleResponse;
import com.allan.mapper.PromotionMapper;
import com.allan.model.Coupon;
import com.allan.model.Promotion;
import com.allan.model.PromotionReward;
import com.allan.model.PromotionRule;
import com.allan.service.CouponService;
import com.allan.service.PromotionService;
import com.allan.service.support.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Seller-scoped promotion and coupon management — every operation here is
 * restricted to promotions/coupons owned by the calling seller.
 *
 * <p><strong>Security architecture:</strong>
 * <ul>
 *   <li><strong>{@code sellerId} always comes from
 *       {@link SecurityUtils#currentSellerId()}</strong> — never from a
 *       path variable, query param, or request body field. There is no
 *       {@code {sellerId}} path segment on this controller at all,
 *       specifically so there's no parameter for a caller to tamper with
 *       to act as a different seller.</li>
 *   <li>Every service call below passes that resolved {@code sellerId}
 *       into {@code PromotionService}/{@code CouponService} methods that
 *       enforce ownership at the query level
 *       ({@code findByIdAndSellerId}) — a seller requesting another
 *       seller's promotion ID gets the same 404 as a nonexistent ID (see
 *       {@code PromotionNotFoundException} javadoc on why those must not
 *       be distinguishable).</li>
 *   <li>Class-level {@code @PreAuthorize("hasRole('SELLER')")} is
 *       defense-in-depth on top of the ownership checks above — adjust the
 *       role name to whatever your security config actually uses.</li>
 *   <li>{@code createPromotion} always goes through
 *       {@code PromotionService.createSellerPromotion}, which itself
 *       rejects {@code PLATFORM_WIDE} scope and out-of-band priority — this
 *       controller doesn't need to re-check those, but doesn't undermine
 *       them either.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/vendor/promotions")
@PreAuthorize("hasRole('SELLER')")
@RequiredArgsConstructor
public class VendorPromotionController {

    private final PromotionService promotionService;
    private final CouponService couponService;
    private final PromotionMapper mapper;

    @PostMapping
    public ResponseEntity<PromotionResponse> create(@Valid @RequestBody PromotionCreateRequest request) {
        Promotion created = promotionService.createSellerPromotion(SecurityUtils.currentSellerId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    @GetMapping("/{id}")
    public PromotionResponse get(@PathVariable Long id) {
        return mapper.toResponse(promotionService.getForSeller(id, SecurityUtils.currentSellerId()));
    }

    @GetMapping
    public Page<PromotionResponse> list(Pageable pageable) {
        return promotionService.listForSeller(SecurityUtils.currentSellerId(), pageable).map(mapper::toResponse);
    }

    @PatchMapping("/{id}/status")
    public PromotionResponse updateStatus(@PathVariable Long id, @RequestParam PromotionStatus status) {
        return mapper.toResponse(promotionService.updateStatus(id, SecurityUtils.currentSellerId(), status));
    }

    @PatchMapping("/{id}/schedule")
    public PromotionResponse updateSchedule(@PathVariable Long id, @RequestParam LocalDateTime endsAt) {
        return mapper.toResponse(promotionService.updateSchedule(id, SecurityUtils.currentSellerId(), endsAt));
    }

    @PatchMapping("/{id}/priority")
    public PromotionResponse updatePriority(@PathVariable Long id, @RequestParam int priority) {
        // Note: PromotionService enforces the 50-200 seller band regardless
        // of what's requested here — this endpoint doesn't get to choose.
        return mapper.toResponse(promotionService.updatePriority(id, SecurityUtils.currentSellerId(), priority));
    }

    @PostMapping("/{id}/rules")
    public ResponseEntity<PromotionRuleResponse> addRule(@PathVariable Long id, @Valid @RequestBody PromotionRuleRequest request) {
        PromotionRule saved = promotionService.addRule(id, SecurityUtils.currentSellerId(), mapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toRuleResponse(saved));
    }

    @DeleteMapping("/{id}/rules/{ruleId}")
    public ResponseEntity<Void> removeRule(@PathVariable Long id, @PathVariable Long ruleId) {
        promotionService.removeRule(id, SecurityUtils.currentSellerId(), ruleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/rewards")
    public ResponseEntity<PromotionRewardResponse> addReward(@PathVariable Long id, @Valid @RequestBody PromotionRewardRequest request) {
        // PromotionServiceImpl additionally rejects a reward whose
        // applicableSellerId points at a different seller than this one —
        // this controller relies on that check rather than re-validating.
        PromotionReward saved = promotionService.addReward(id, SecurityUtils.currentSellerId(), mapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toRewardResponse(saved));
    }

    @DeleteMapping("/{id}/rewards/{rewardId}")
    public ResponseEntity<Void> removeReward(@PathVariable Long id, @PathVariable Long rewardId) {
        promotionService.removeReward(id, SecurityUtils.currentSellerId(), rewardId);
        return ResponseEntity.noContent().build();
    }

    // ── Coupons ──────────────────────────────────────────────────────────

    @PostMapping("/{id}/coupons")
    public ResponseEntity<CouponResponse> createCoupon(@PathVariable Long id, @Valid @RequestBody CouponCreateRequest request) {
        Coupon created = couponService.createCoupon(id, SecurityUtils.currentSellerId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    @PostMapping("/{id}/coupons/bulk")
    public ResponseEntity<List<CouponResponse>> bulkGenerateCoupons(@PathVariable Long id,
                                                                      @RequestParam int count,
                                                                      @Valid @RequestBody CouponCreateRequest template) {
        List<Coupon> created = couponService.bulkGenerateCoupons(id, SecurityUtils.currentSellerId(), count, template);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toCouponResponseList(created));
    }

    @GetMapping("/{id}/coupons")
    public Page<CouponResponse> listCoupons(@PathVariable Long id, Pageable pageable) {
        return couponService.findByPromotionId(id, SecurityUtils.currentSellerId(), pageable).map(mapper::toResponse);
    }

    @PostMapping("/coupons/{couponId}/disable")
    public ResponseEntity<Void> disableCoupon(@PathVariable Long couponId, @RequestParam(required = false) String reason) {
        couponService.disableCoupon(couponId, SecurityUtils.currentSellerId(), reason);
        return ResponseEntity.noContent().build();
    }
}