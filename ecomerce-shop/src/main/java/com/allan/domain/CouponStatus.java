package com.allan.domain;

public enum CouponStatus {
    ACTIVE,     // accepting redemptions (subject to usage limits)
    EXHAUSTED,  // usedCount == usageLimit; no further redemptions possible
    EXPIRED,    // parent promotion has EXPIRED or CANCELLED
    DISABLED    // manually deactivated by admin or vendor before exhaustion
}