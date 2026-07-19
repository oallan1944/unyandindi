package com.allan.domain;

public enum PromotionType {
    PERCENTAGE_OFF,   // e.g. 20% off eligible cart total
    FLAT_AMOUNT_OFF,  // e.g. UGX 2,000 off eligible cart total
    FREE_SHIPPING,    // zeroes the shipping fee at checkout
    BUY_X_GET_Y       // buy N items, get M free — governed by rule + reward pair
}