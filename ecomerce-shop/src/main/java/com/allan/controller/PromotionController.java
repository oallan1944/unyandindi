package com.allan.controller;

import com.allan.model.PromotionReward;
import com.allan.model.PromotionRule;
import com.allan.domain.PromotionStatus;
import com.allan.dto.CartContext;
import com.allan.dto.PromotionApplicationResult;
import com.allan.dto.PromotionCreateRequest;
import com.allan.request.PromotionRewardRequest;
import com.allan.request.PromotionRuleRequest;
import com.allan.response.PromotionResponse;
import com.allan.response.PromotionRewardResponse;
import com.allan.response.PromotionRuleResponse;
import com.allan.request.ValidateCouponRequest;
import com.allan.mapper.PromotionMapper;
import com.allan.model.Promotion;
import com.allan.service.PromotionEvaluatorService;
import com.allan.service.PromotionService;
import com.allan.service.support.CartContextBuilder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Admin-only promotion management: CRUD, status transitions, and a
 * side-effect-free preview endpoint.
 *
 * <p><strong>Security architecture:</strong>
 * <ul>
 *   <li>Class-level {@code @PreAuthorize("hasRole('ADMIN')")} — every
 *       endpoint here requires the admin role. This is defense-in-depth on
 *       top of whatever gateway/network-level restriction also applies;
 *       don't rely on this annotation alone without
 *       {@code @EnableMethodSecurity} actually being active.</li>
 *   <li>{@code sellerId} is never accepted here — all promotions created
 *       through this controller are platform-wide
 *       ({@code PromotionService.createPlatformPromotion}), and status/
 *       schedule/priority updates resolve the promotion via plain
 *       {@code getForAdmin}/service calls with {@code sellerId = null},
 *       which is the admin-bypass path documented on
 *       {@code PromotionServiceImpl.resolveForWrite}.</li>
 *   <li>{@link #preview} calls {@code PromotionEvaluatorService} read-only —
 *       it never redeems anything, so admins can safely dry-run a cart
 *       against live promotions without side effects.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/admin/promotions")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;
    private final PromotionEvaluatorService promotionEvaluatorService;
    private final CartContextBuilder cartContextBuilder;
    private final PromotionMapper mapper;

    @PostMapping
    public ResponseEntity<PromotionResponse> create(@Valid @RequestBody PromotionCreateRequest request) {
        Promotion created = promotionService.createPlatformPromotion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    @GetMapping("/{id}")
    public PromotionResponse get(@PathVariable Long id) {
        return mapper.toResponse(promotionService.getForAdmin(id));
    }

    @GetMapping
    public Page<PromotionResponse> listPlatformPromotions(Pageable pageable) {
        return promotionService.listPlatformPromotions(pageable).map(mapper::toResponse);
    }

    @GetMapping("/by-status")
    public Page<PromotionResponse> listByStatus(@RequestParam PromotionStatus status, Pageable pageable) {
        return promotionService.listByStatus(status, pageable).map(mapper::toResponse);
    }

    @PatchMapping("/{id}/status")
    public PromotionResponse updateStatus(@PathVariable Long id, @RequestParam PromotionStatus status) {
        return mapper.toResponse(promotionService.updateStatus(id, null, status));
    }

    /** Convenience alias — same effect as {@code PATCH .../status?status=ACTIVE}. */
    @PostMapping("/{id}/activate")
    public PromotionResponse activate(@PathVariable Long id) {
        return mapper.toResponse(promotionService.updateStatus(id, null, PromotionStatus.ACTIVE));
    }

    @PostMapping("/{id}/pause")
    public PromotionResponse pause(@PathVariable Long id) {
        return mapper.toResponse(promotionService.updateStatus(id, null, PromotionStatus.PAUSED));
    }

    /**
     * ASSUMPTION: no separate ARCHIVED status exists — EXPIRED is used as
     * the terminal/retired state whether a promotion ended by date or was
     * manually retired by an admin. See PromotionServiceImpl javadoc.
     */
    @PostMapping("/{id}/archive")
    public PromotionResponse archive(@PathVariable Long id) {
        return mapper.toResponse(promotionService.updateStatus(id, null, PromotionStatus.EXPIRED));
    }

    @PatchMapping("/{id}/schedule")
    public PromotionResponse updateSchedule(@PathVariable Long id, @RequestParam LocalDateTime endsAt) {
        return mapper.toResponse(promotionService.updateSchedule(id, null, endsAt));
    }

    @PatchMapping("/{id}/priority")
    public PromotionResponse updatePriority(@PathVariable Long id, @RequestParam int priority) {
        return mapper.toResponse(promotionService.updatePriority(id, null, priority));
    }

    @PostMapping("/{id}/rules")
    public ResponseEntity<PromotionRuleResponse> addRule(@PathVariable Long id, @Valid @RequestBody PromotionRuleRequest request) {
        PromotionRule saved = promotionService.addRule(id, null, mapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toRuleResponse(saved));
    }

    @DeleteMapping("/{id}/rules/{ruleId}")
    public ResponseEntity<Void> removeRule(@PathVariable Long id, @PathVariable Long ruleId) {
        promotionService.removeRule(id, null, ruleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/rewards")
    public ResponseEntity<PromotionRewardResponse> addReward(@PathVariable Long id, @Valid @RequestBody PromotionRewardRequest request) {
        PromotionReward saved = promotionService.addReward(id, null, mapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toRewardResponse(saved));
    }

    @DeleteMapping("/{id}/rewards/{rewardId}")
    public ResponseEntity<Void> removeReward(@PathVariable Long id, @PathVariable Long rewardId) {
        promotionService.removeReward(id, null, rewardId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Dry-run: evaluates a hypothetical cart against all live promotions
     * without redeeming anything. {@code userId} is intentionally not
     * required — admins previewing promotion behavior aren't redeeming on
     * behalf of a real customer, so per-user coupon limits are not checked
     * here (the response reflects promotion/rule eligibility only).
     */
    @PostMapping("/preview")
    public PromotionApplicationResult preview(@Valid @RequestBody ValidateCouponRequest request) {
        CartContext cart = cartContextBuilder.buildFromLines(null, request.couponCode(), request.items());
        return request.couponCode() == null || request.couponCode().isBlank()
                ? promotionEvaluatorService.evaluate(cart)
                : promotionEvaluatorService.evaluateWithCoupon(cart, request.couponCode());
    }
}