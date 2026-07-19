package com.allan.domain;


public enum PromotionStatus {
    DRAFT,      // created but not published; fully editable
    ACTIVE,     // live and accepting redemptions
    PAUSED,     // temporarily disabled; can be re-activated
    EXPIRED,    // past endsAt; set automatically by PromotionSchedulerService
    CANCELLED   // permanently closed; no further redemptions possible
}
