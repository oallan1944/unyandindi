package com.allan.controller;

import com.allan.dto.CartContext;
import com.allan.response.ValidateCouponResponse;
import com.allan.dto.PromotionEvaluationResult;
import com.allan.request.RedeemCouponRequest;
import com.allan.dto.RedemptionOutcome;
import com.allan.response.RedemptionResponse;
import com.allan.request.ValidateCouponRequest;
import com.allan.exceptions.CouponRedemptionException;
import com.allan.service.CouponService;
import com.allan.service.RedemptionService;
import com.allan.service.support.CartContextBuilder;
import com.allan.service.support.OrderContextService;
import com.allan.service.support.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Customer-facing coupon validation and redemption.
 *
 * <p><strong>Security architecture:</strong>
 * <ul>
 *   <li><strong>{@code userId} always comes from
 *       {@link SecurityUtils#currentUserId()}</strong> — never from the
 *       request body.</li>
 *   <li><strong>Two different cart-construction paths, deliberately:</strong>
 *       {@link #validate} builds its {@code CartContext} from client-supplied
 *       {@code productId}/{@code quantity} pairs via {@code CartContextBuilder}
 *       (appropriate pre-order, since no order exists yet to be authoritative).
 *       {@link #redeem} instead rebuilds from the already-persisted
 *       {@code Order} via {@code OrderContextService} — it does NOT accept
 *       cart items in the request at all, closing the door on a client
 *       resubmitting different prices/quantities at the final redeem step
 *       than what was actually ordered.</li>
 *   <li><strong>Exceptions never leak internals to the customer.</strong>
 *       {@link CouponRedemptionException} carries a {@code Reason} enum for
 *       server-side logging/metrics, but the HTTP response body only ever
 *       contains a generic, safe message via {@link ValidateCouponResponse#failure}
 *       — see {@link #handleRedemptionException}.</li>
 *   <li>Both endpoints require authentication (no {@code @PreAuthorize}
 *       role restriction beyond "must be logged in" — adjust if guest
 *       checkout with coupons is out of scope for your product).</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/checkout/coupons")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class CheckoutCouponController {

    private final CouponService couponService;
    private final RedemptionService redemptionService;
    private final CartContextBuilder cartContextBuilder;
    private final OrderContextService orderContextService;

    @PostMapping("/validate")
    public ValidateCouponResponse validate(@Valid @RequestBody ValidateCouponRequest request) {
        Long userId = SecurityUtils.currentUserId();
        CartContext cart = cartContextBuilder.buildFromLines(userId, request.couponCode(), request.items());

        // CouponService.validate is read-only — safe to call as often as
        // the customer edits their cart, with no risk of consuming usage.
        PromotionEvaluationResult result = couponService.validate(request.couponCode(), cart);

        if (!result.eligible()) {
            // TODO: ruleOutcomes on PromotionEvaluationResult likely carries the
            // specific rejection reason (e.g. "minimum spend not met") — surface
            // that here once RuleEvaluationOutcome's shape is available, instead
            // of this generic message.
            return ValidateCouponResponse.failure("This coupon can't be applied to your order.");
        }

        // rewardLabel has no equivalent on PromotionEvaluationResult (promotionId,
        // eligible, discountAmount, ruleOutcomes only) — passed null until/unless
        // a display label is added there or looked up separately.
        return ValidateCouponResponse.success(
                result.discountAmount(),
                cart.subtotal() - result.discountAmount(),
                result.promotionId(),
                null
        );
    }

    @PostMapping("/redeem")
    public RedemptionResponse redeem(@Valid @RequestBody RedeemCouponRequest request) {
        Long userId = SecurityUtils.currentUserId();

        // Ownership of the order is verified inside OrderContextService —
        // see its javadoc. The cart used for the final discount is the
        // order's own persisted line items, not anything resubmitted here.
        CartContext cart = orderContextService.loadForRedemption(request.getOrderId(), userId);

        RedemptionOutcome outcome = redemptionService.redeem(request.getCouponCode(), userId, request.getOrderId(), cart);
        return RedemptionResponse.of(outcome);
    }

    @ExceptionHandler(CouponRedemptionException.class)
    public ResponseEntity<ValidateCouponResponse> handleRedemptionException(CouponRedemptionException ex) {
        // Log ex.getReason() + ex.getMessage() server-side (via your usual
        // logging setup) for support/fraud investigation — but the
        // customer only ever sees a generic, non-leaky message. Do not
        // return ex.getMessage() or ex.getReason() directly in the body.
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ValidateCouponResponse.failure("This coupon can't be applied to your order"));
    }
}