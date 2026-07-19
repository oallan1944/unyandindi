package com.allan.repository;

import com.allan.model.PromotionReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link PromotionReward}.
 *
 * <p><strong>Security note:</strong> as with {@code PromotionRuleRepository},
 * the "immutable after redemptions" contract on {@link PromotionReward} is
 * enforced by {@code PromotionServiceImpl}, not by this repository. Do not
 * bypass the service layer to mutate rewards on a promotion that already has
 * confirmed redemptions.
 */
@Repository
public interface PromotionRewardRepository extends JpaRepository<PromotionReward, Long> {

    List<PromotionReward> findByPromotionId(Long promotionId);

    /**
     * Ownership-scoped variant for seller-facing read endpoints.
     *
     * <p>{@code Promotion.vendorId} stores a {@link com.allan.model.Seller}
     * ID — there is no {@code Vendor} entity — hence the explicit
     * {@code @Query} rather than a derived {@code ...VendorId} method name.
     */
    @Query("select r from PromotionReward r " +
           "where r.promotion.id = :promotionId and r.promotion.sellerId = :sellerId")
    List<PromotionReward> findByPromotionIdAndPromotionSellerId(@Param("promotionId") Long promotionId,
                                                                  @Param("sellerId") Long sellerId);
}