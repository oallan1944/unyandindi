package com.allan.engine.Rules;

import com.allan.model.Cart;
import com.allan.model.PromotionRule;
import com.allan.model.User;

public interface CartRule {

    /**
     * Returns true if this rule passes for the given cart and user.
     * Throwing is not permitted — return false and log a warning instead.
     */
    boolean evaluate(Cart cart, User user, PromotionRule rule);
}