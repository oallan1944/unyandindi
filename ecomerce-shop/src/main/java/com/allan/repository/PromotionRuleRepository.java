package com.allan.repository;

import com.allan.model.PromotionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link PromotionRule}.
 *
 * <p><strong>Security note:</strong> this repository does not enforce the
 * "immutable after redemptions" contract described on {@link PromotionRule}
 * — that check requires knowledge of {@code CouponRedemption} existence and
 * belongs in {@code PromotionServiceImpl}, not here. Do not call
 * {@code save}/{@code delete} on this repository directly from a controller;
 * always go through the service layer so that guard is applied.
 */
@Repository
public interface PromotionRuleRepository extends JpaRepository<PromotionRule, Long> {

    List<PromotionRule> findByPromotionId(Long promotionId);

    /**
     * Ownership-scoped variant for seller-facing read endpoints, avoiding a
     * separate promotion-ownership lookup before returning rule data.
     *
     * <p>{@code Promotion.sellerId} stores a {@link com.allan.model.Seller}
     * ID — there is no {@code Vendor} entity — hence the explicit
     * {@code @Query} rather than a derived {@code ...SellerId } method name.
     */
    @Query("select r from PromotionRule r " +
           "where r.promotion.id = :promotionId and r.promotion.sellerId = :sellerId")
    List<PromotionRule> findByPromotionIdAndPromotionSellerId(@Param("promotionId") Long promotionId,
                                                                @Param("sellerId") Long sellerId);
}