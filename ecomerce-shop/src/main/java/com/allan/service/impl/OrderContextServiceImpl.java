package com.allan.service.impl;

import com.allan.dto.CartContext;
import com.allan.dto.CartItem;
import com.allan.model.Order;
import com.allan.model.OrderItem;
import com.allan.model.Product;
import com.allan.repository.OrderRepository;
import com.allan.service.support.OrderContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Builds a trusted immutable CartContext from a persisted Order.
 *
 * <p>Security guarantees:
 * <ul>
 *     <li>Order ownership is validated before any cart data is exposed.</li>
 *     <li>All monetary values originate from frozen OrderItem prices.</li>
 *     <li>Live Product prices are never used.</li>
 *     <li>Product is consulted only for seller/category classification.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderContextServiceImpl implements OrderContextService {

    private final OrderRepository orderRepository;

    @Override
    public CartContext loadForRedemption(Long orderId, Long userId) {

        if (orderId == null) {
            throw new IllegalArgumentException("orderId is required");
        }

        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }

        Order order = orderRepository.findByIdWithItemsAndProducts(orderId)
                .orElseThrow(() -> {
                    log.warn("Order {} not found", orderId);
                    return new IllegalArgumentException(
                            "Order not found: " + orderId);
                });

        validateOwnership(order, userId);

        List<CartItem> items = order.getOrderItems()
                .stream()
                .map(this::toCartItem)
                .toList();

        long subtotal = order.getOrderItems()
                .stream()
                .mapToLong(this::lineTotal)
                .sum();

        log.info(
                "Built redemption CartContext [orderId={}, userId={}, items={}, subtotal={}]",
                orderId,
                userId,
                items.size(),
                subtotal
        );

        /*
         * Coupon code is intentionally null.
         * RedemptionService should populate it from the redemption request,
         * not from the Order entity.
         */
        return CartContext.of(userId, items, subtotal);
    }

    /**
     * Maps an OrderItem into an immutable CartItem.
     */
    private CartItem toCartItem(OrderItem item) {

        Product product = item.getProduct();

        if (product == null) {
            throw new IllegalStateException(
                    "OrderItem " + item.getId() + " has no Product");
        }

        long unitPrice = item.getSellingPrice() == null
                ? 0L
                : item.getSellingPrice();

        return new CartItem(
                product.getId(),
                product.getSeller() == null
                        ? null
                        : product.getSeller().getId(),
                product.getCategory() == null
                        ? null
                        : product.getCategory().getId(),
                item.getQuantity(),
                unitPrice,
                unitPrice * item.getQuantity()
        );
    }

    /**
     * Uses only frozen OrderItem prices.
     */
    private long lineTotal(OrderItem item) {

        long unitPrice = item.getSellingPrice() == null
                ? 0L
                : item.getSellingPrice();

        return unitPrice * item.getQuantity();
    }

    /**
     * Prevents coupon redemption against another customer's order.
     */
    private void validateOwnership(Order order, Long userId) {

        Long ownerId = order.getUser().getId();

        if (!ownerId.equals(userId)) {

            log.warn(
                    "User {} attempted to redeem coupon for order {} owned by {}",
                    userId,
                    order.getId(),
                    ownerId
            );

            throw new IllegalArgumentException(
                    "Order does not belong to authenticated user");
        }
    }
}