package com.allan.dto;

import com.allan.domain.CouponStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Internal DTO carrying coupon summary data from the mapper to the controller.
 * Produced by {@link com.allan.mapper.PromotionMapper#toCouponDTO}.
 * Returned directly in {@code ResponseEntity<CouponDTO>}.
 *
 * <p>Intentionally omits:
 * <ul>
 *   <li>{@code version} — optimistic lock field, never exposed.</li>
 *   <li>Full {@code redemptions} list — use {@link CouponUsageDTO} for analytics.</li>
 *   <li>Full parent {@link PromotionDTO} — only {@code promotionId} and
 *       {@code promotionName} included. Fetch full promotion separately if needed.</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
public class CouponDTO {

    private Long id;
    private String code;
    private CouponStatus status;

    /** Parent promotion ID — use to fetch full promotion details. */
    private Long promotionId;

    /** Parent promotion name — for display without an extra request. */
    private String promotionName;

    /** Maximum total redemptions. Null = unlimited. */
    private Integer usageLimit;

    /** Current confirmed redemption count. */
    private int usedCount;

    /** Maximum redemptions per individual customer. Null = no cap. */
    private Integer usagePerCustomer;

    /**
     * Remaining global uses.
     * {@link Integer#MAX_VALUE} when usageLimit is null (unlimited).
     * Computed by mapper from entity domain helpers — not a persisted field.
     */
    private int remainingUses;

    /** Whether this coupon can currently accept redemptions. */
    private boolean available;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}