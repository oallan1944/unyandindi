package com.allan.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.allan.domain.OrderStatus;
import com.allan.dto.OrderDTO;
import com.allan.model.Seller;
import com.allan.service.OrderService;
import com.allan.service.SellerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Seller-facing order endpoints.
 *
 * Restricted to ROLE_SELLER via SecurityConfig. A seller only ever sees
 * their own orders — scoping is enforced here by resolving the seller's
 * identity from their JWT before querying, never from a client-supplied ID.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller/orders")
public class SellerOrderController {

    private final OrderService orderService;
    private final SellerService sellerService;

    /** List all orders belonging to the authenticated seller. */
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrdersHandler(
            @RequestHeader("Authorization") String jwt) throws Exception {
        Seller seller = sellerService.getSellerProfile(jwt);
        List<OrderDTO> orders = orderService.sellersOrder(seller.getId());
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    /** Update the status of one of the authenticated seller's orders. */
    @PatchMapping("/{orderId}/status/{status}")
    public ResponseEntity<OrderDTO> updateOrderHandler(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long orderId,
            @PathVariable String status) throws Exception {

        // ✅ resolve seller from JWT — confirms caller owns this order before mutating it
        Seller seller = sellerService.getSellerProfile(jwt);

        OrderStatus orderStatus;
        try {
            orderStatus = OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Seller {} attempted invalid order status: {}", seller.getId(), status);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        OrderDTO order = orderService.updateOrderStatus(orderId, orderStatus);

        // ✅ ownership check — a seller must not be able to update another seller's order
        // by guessing an orderId. OrderDTO carries sellerId for exactly this check.
        if (!seller.getId().equals(order.getSellerId())) {
            log.warn("Seller {} attempted to update order {} belonging to a different seller",
                    seller.getId(), orderId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        log.info("Seller {} updated order {} to status {}", seller.getId(), orderId, orderStatus);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }
}