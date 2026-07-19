package com.allan.domain;

public enum PromotionScope {
    PLATFORM_WIDE,    // applies to all items from all vendors — admin only
    VENDOR_SPECIFIC,  // applies only to items where product.seller.id == vendorId
    CATEGORY,         // applies to items where product.category.id matches rule value
    PRODUCT           // applies to specific product IDs listed in a PromotionRule
}
