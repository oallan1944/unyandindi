package com.allan.domain;

public enum RuleType {
    MIN_ORDER_VALUE,   // cart subtotal (shillings) >= value
    MIN_ITEM_COUNT,    // total item quantity in cart >= value
    PRODUCT_IN_CART,   // at least one of the listed product IDs must be present
    CATEGORY,          // cart must contain a product from Category.id == value
    USER_SEGMENT,      // customer belongs to the named segment (NEW_CUSTOMER, VIP…)
    FIRST_ORDER_ONLY,  // customer has zero prior completed orders
    VENDOR_PRODUCTS    // cart must contain product(s) from Seller.id == value
}