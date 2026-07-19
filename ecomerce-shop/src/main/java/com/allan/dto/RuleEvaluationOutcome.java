package com.allan.dto;

/**
 * Result of evaluating a single {@code PromotionRule} against a
 * {@code CartContext}.
 *
 * <p>Intended for internal diagnostics (admin "why isn't this promotion
 * applying" tooling, audit notes). {@code reason} is meant for
 * merchant/support-facing display — do not surface raw rule internals
 * (thresholds, IDs) directly to end customers, since that can leak
 * competitor pricing strategy or invite gaming of thresholds.
 */
public record RuleEvaluationOutcome(
        Long ruleId,
        boolean passed,
        String reason
) {
}