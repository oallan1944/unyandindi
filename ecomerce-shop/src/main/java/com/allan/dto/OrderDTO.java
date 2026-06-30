package com.allan.dto;

import com.allan.domain.OrderStatus;
import com.allan.domain.PaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private String orderId;
    private OrderStatus orderStatus;
    private double totalMrpPrice;
    private Integer totalSellingPrice;
    private Integer discount;
    private int totalItem;
    private PaymentStatus paymentStatus;
    private LocalDateTime orderDate;
    private LocalDateTime deliverDate;

    // ✅ only the user fields needed for order display
    private UserSummaryDTO user;

    // Needed for cancel flow controller
    private Long sellerId;


    // ✅ only the address fields needed
    private AddressSummaryDTO shippingAddress;

    // ✅ order items included since they're always shown with an order
    private List<OrderItemDTO> orderItems;
}
