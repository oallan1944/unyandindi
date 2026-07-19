package com.allan.dto;

import java.util.List;

/**
 * Outcome of evaluating one {@code Promotion} against a {@code CartContext}:
 * whether it's eligible, and if so, the discount it would produce.
 *
 * <p>{@code discountAmount} here is already capped against the promotion's
 * {@code maximumDiscountAmount} and clamped to never exceed
 * {@code CartContext.subtotal()} — {@code PromotionEvaluatorService} and
 * {@code RedemptionService} must treat this as the authoritative figure and
 * never let a caller pass in its own discount value.
 */
public record PromotionEvaluationResult(
        Long promotionId,
        boolean eligible,
        long discountAmount,
        List<RuleEvaluationOutcome> ruleOutcomes
) {
    public static PromotionEvaluationResult ineligible(Long promotionId, List<RuleEvaluationOutcome> outcomes) {
        return new PromotionEvaluationResult(promotionId, false, 0L, outcomes);
    }

    public static PromotionEvaluationResult eligible(Long promotionId, long discountAmount,
                                                       List<RuleEvaluationOutcome> outcomes) {
        return new PromotionEvaluationResult(promotionId, true, discountAmount, outcomes);
    }
}