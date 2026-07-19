package com.allan.request;

import com.allan.domain.RewardType;
import jakarta.validation.constraints.NotNull;

/**
 * Inbound payload for adding a reward to a promotion. Same rationale as
 * {@link PromotionRuleRequest} — replaces direct {@code PromotionReward}
 * entity binding, which would expose {@code id} and the {@code promotion}
 * association to client control.
 */
public record PromotionRewardRequest(
        @NotNull RewardType rewardType,
        long value,
        String label,
        Long applicableSellerId
) {
}