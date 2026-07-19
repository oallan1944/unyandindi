package com.allan.domain;

public enum RewardType {
    PERCENTAGE_OFF,  // value = whole-number percent (e.g. 20 = 20% off)
    FLAT_OFF,        // value = fixed discount in shillings
    FREE_SHIPPING,   // value = 0 (unused); shipping fee zeroed at checkout
    FREE_ITEM        // value = Product.id of the item added free at checkout
}