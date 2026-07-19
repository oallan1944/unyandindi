package com.allan.request;

import com.allan.domain.PromotionStatus;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Inbound request body for partially updating an existing promotion.
 * Sent via PATCH /api/admin/promotions/{id} or
 * PATCH /api/vendor/promotions/{id}.
 *
 * <p>Only non-null fields are applied — null means "leave unchanged".
 * Fields excluded from this request ({@code type}, {@code scope},
 * {@code rules}, {@code rewards}) are immutable once any confirmed
 * {@link com.allan.model.CouponRedemption} exists for the promotion.
 *
 * <p><strong>Allowed status transitions:</strong>
 * <pre>
 *   DRAFT   → ACTIVE     publish
 *   ACTIVE  → PAUSED     temporarily suspend
 *   PAUSED  → ACTIVE     resume
 *   ACTIVE  → CANCELLED  permanently close
 *   PAUSED  → CANCELLED  permanently close
 * </pre>
 * Submitting {@code EXPIRED} is rejected — expiry is system-only,
 * set automatically by the promotion scheduler.
 */
@Getter
@Setter
@NoArgsConstructor
public class UpdatePromotionRequest {

    @Size(max = 150, message = "Promotion name must not exceed 150 characters.")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters.")
    private String description;

    /**
     * Target status. Null = no status change.
     * EXPIRED is rejected — managed by scheduler only.
     */
    private PromotionStatus status;

    @Min(value = 1, message = "Priority must be at least 1.")
    @Max(value = 999, message = "Priority must not exceed 999.")
    private Integer priority;

    private Boolean stackable;

    private Boolean exclusive;

    /**
     * Updated minimum cart total in UGX whole shillings. Null = no change.
     */
    @Min(value = 0, message = "Minimum order value must not be negative.")
    private Long minimumOrderValue;

    /**
     * Updated maximum discount cap in UGX whole shillings.
     * Null = no change. 0 = remove cap entirely.
     */
    @Min(value = 0, message = "Maximum discount amount must not be negative.")
    private Long maximumDiscountAmount;

    /**
     * Updated end datetime. Must be in the future.
     * Null = no change.
     * Shortening is permitted unless it would place endsAt before now.
     */
    @Future(message = "End date must be in the future.")
    private LocalDateTime endsAt;

    /**
     * Updated start datetime. Null = no change.
     * Can only be changed while status is DRAFT — rejected by service
     * if promotion is ACTIVE or has redemptions.
     */
    private LocalDateTime startsAt;
}