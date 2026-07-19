package com.allan.request;

import com.allan.domain.PromotionScope;
import com.allan.domain.PromotionType;
import com.allan.domain.RewardType;
import com.allan.domain.RuleOperator;
import com.allan.domain.RuleType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Inbound request body for creating a new promotion.
 * Sent by admin or vendor client via POST /api/admin/promotions
 * or POST /api/vendor/promotions.
 *
 * <p>The service layer enforces scope restrictions — vendors cannot
 * set {@code scope = PLATFORM_WIDE} regardless of what they submit.
 *
 * <p>All monetary fields are UGX whole shillings ({@code long}).
 * No decimals, no currency symbol. Example: {@code 50000} = UGX 50,000.
 *
 * <p>Requires {@code @Valid} on the controller {@code @RequestBody}
 * parameter and {@code @Validated} on the controller class for
 * Bean Validation constraints to fire.
 */
@Getter
@Setter
@NoArgsConstructor
public class CreatePromotionRequest {

    // ── Identity ──────────────────────────────────────────────────────────────

    @NotBlank(message = "Promotion name is required.")
    @Size(max = 150, message = "Promotion name must not exceed 150 characters.")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters.")
    private String description;

    @NotNull(message = "Promotion type is required.")
    private PromotionType type;

    @NotNull(message = "Promotion scope is required.")
    private PromotionScope scope;

    // ── Application logic ─────────────────────────────────────────────────────

    /**
     * Lower number = higher precedence at checkout.
     * Admin promotions: 1–10. Vendor promotions: 50–200.
     */
    @Min(value = 1, message = "Priority must be at least 1.")
    @Max(value = 999, message = "Priority must not exceed 999.")
    private int priority = 100;

    private boolean stackable = false;

    private boolean exclusive = false;

    /**
     * Minimum cart total in UGX (whole shillings). 0 = no minimum.
     */
    @Min(value = 0, message = "Minimum order value must not be negative.")
    private long minimumOrderValue = 0L;

    /**
     * Hard cap on computed UGX discount. 0 = no cap.
     */
    @Min(value = 0, message = "Maximum discount amount must not be negative.")
    private long maximumDiscountAmount = 0L;

    // ── Schedule ──────────────────────────────────────────────────────────────

    @NotNull(message = "Start date and time is required.")
    @Future(message = "Start date must be in the future.")
    private LocalDateTime startsAt;

    @NotNull(message = "End date and time is required.")
    @Future(message = "End date must be in the future.")
    private LocalDateTime endsAt;

    // ── Rules ─────────────────────────────────────────────────────────────────

    /**
     * Eligibility rules (AND semantics — all must pass).
     * Empty list = promotion applies unconditionally to any cart.
     */
    @Valid
    private List<PromotionRuleRequest> rules = new ArrayList<>();

    // ── Rewards ───────────────────────────────────────────────────────────────

    @NotEmpty(message = "At least one reward must be defined.")
    @Valid
    private List<PromotionRewardRequest> rewards = new ArrayList<>();

    // ── Nested: rule ──────────────────────────────────────────────────────────

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PromotionRuleRequest {

        @NotNull(message = "Rule type is required.")
        private RuleType ruleType;

        @NotNull(message = "Rule operator is required.")
        private RuleOperator operator;

        /**
         * Threshold or reference value as a plain string.
         * Format depends on ruleType. Examples:
         * "50000" for MIN_ORDER_VALUE, "101,204" for PRODUCT_IN_CART.
         */
        @NotBlank(message = "Rule value is required.")
        @Size(max = 500, message = "Rule value must not exceed 500 characters.")
        private String value;

        @Size(max = 255, message = "Description must not exceed 255 characters.")
        private String description;
    }

    // ── Nested: reward ────────────────────────────────────────────────────────

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PromotionRewardRequest {

        @NotNull(message = "Reward type is required.")
        private RewardType rewardType;

        /**
         * PERCENTAGE_OFF: whole-number percent (1–100).
         * FLAT_OFF: UGX whole shillings (> 0).
         * FREE_SHIPPING: store 0 (unused).
         * FREE_ITEM: the Product ID to add free.
         */
        @Min(value = 0, message = "Reward value must not be negative.")
        private long value;

        @Size(max = 255, message = "Label must not exceed 255 characters.")
        private String label;

        /** NULL = reward applies to all sellers in scope. */
        private Long applicableSellerId;
    }
}