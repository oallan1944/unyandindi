package com.allan.request;

import com.allan.domain.RuleOperator;
import com.allan.domain.RuleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Inbound payload for adding a rule to a promotion.
 *
 * <p><strong>Security:</strong> replaces binding {@code @RequestBody PromotionRule}
 * directly, which would let a client set {@code id} and — via the JPA
 * relationship — potentially the {@code promotion} association itself. Only
 * the fields a client legitimately supplies are here; the controller/service
 * attaches the actual {@code Promotion} association server-side.
 */
public record PromotionRuleRequest(
        @NotNull RuleType ruleType,
        @NotNull RuleOperator operator,
        @NotBlank String value,
        String description
) {
}