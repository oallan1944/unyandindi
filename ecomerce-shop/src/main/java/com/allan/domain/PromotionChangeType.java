package com.allan.domain;

/**
 * Kind of change that triggered a {@code PromotionChangedEvent}.
 * Mirrors the transitions exposed by {@code PromotionController}
 * ({@code activate}, {@code pause}, {@code archive}) plus generic rule/reward/
 * schedule edits.
 */
public enum PromotionChangeType {
    ACTIVATED,
    PAUSED,
    UPDATED,
    EXPIRED,
    ARCHIVED
}