package com.allan.dto;

import com.allan.domain.PromotionScope;
import com.allan.domain.PromotionStatus;
import com.allan.domain.PromotionType;
import com.allan.domain.RewardType;
import com.allan.domain.RuleOperator;
import com.allan.domain.RuleType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Internal DTO carrying full promotion data from the mapper to the controller.
 * Produced by {@link com.allan.mapper.PromotionMapper#toPromotionDTO}.
 *
 * <p>The controller returns this directly in {@code ResponseEntity<PromotionDTO>}.
 * It is the authoritative promotion shape at the API boundary — the
 * {@link com.allan.model.Promotion} entity is never serialized directly.
 *
 * <p>Omits internal fields: {@code version} (optimistic lock),
 * raw lazy collections. Monetary fields are UGX whole shillings ({@code long}).
 */
@Getter
@Setter
@NoArgsConstructor
public class PromotionDTO {

    private Long id;
    private Long vendorId;
    private String name;
    private String description;
    private PromotionType type;
    private PromotionScope scope;
    private PromotionStatus status;
    private Integer priority;
    private boolean stackable;
    private boolean exclusive;

    /** Minimum cart total in UGX whole shillings. 0 = no minimum. */
    private long minimumOrderValue;

    /** Maximum UGX discount cap. 0 = no cap. */
    private long maximumDiscountAmount;

    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;

    /** True if the promotion is currently within its active window. */
    private boolean live;

    /** True if this promotion requires a coupon code to be applied. */
    private boolean codeBased;

    /** Total coupons generated under this promotion. */
    private int totalCoupons;

    /** Sum of usedCount across all coupons. */
    private int totalRedemptions;

    private List<RuleDTO> rules;
    private List<RewardDTO> rewards;

    // ── Nested: rule ──────────────────────────────────────────────────────────

    @Getter
    @Setter
    @NoArgsConstructor
    public static class RuleDTO {
        private Long id;
        private RuleType ruleType;
        private RuleOperator operator;
        private String value;
        private String description;
    }

    // ── Nested: reward ────────────────────────────────────────────────────────

    @Getter
    @Setter
    @NoArgsConstructor
    public static class RewardDTO {
        private Long id;
        private RewardType rewardType;

        /**
         * PERCENTAGE_OFF: whole-number percent (e.g. 20 = 20% off).
         * FLAT_OFF: UGX whole shillings.
         * FREE_SHIPPING: 0 (unused).
         * FREE_ITEM: Product ID of the free item.
         */
        private long value;
        private String label;
        private Long applicableVendorId;
    }
}