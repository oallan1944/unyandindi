package com.allan.engine;

import com.allan.domain.RuleType;
import com.allan.engine.Rules.*;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RuleFactory {

    private final MinOrderValueRule minOrderValueRule;
    private final MinItemCountRule minItemCountRule;
    private final ProductInCartRule productInCartRule;
    private final CategoryRule categoryRule;
    private final UserSegmentRule userSegmentRule;
    private final FirstOrderOnlyRule firstOrderOnlyRule;
    private final VendorProductsRule vendorProductsRule;

    public CartRule getRule(RuleType type) {
        return switch (type) {
            case MIN_ORDER_VALUE  -> minOrderValueRule;
            case MIN_ITEM_COUNT   -> minItemCountRule;
            case PRODUCT_IN_CART  -> productInCartRule;
            case CATEGORY         -> categoryRule;
            case USER_SEGMENT     -> userSegmentRule;
            case FIRST_ORDER_ONLY -> firstOrderOnlyRule;
            case VENDOR_PRODUCTS  -> vendorProductsRule;
        };
    }
}
