// src/main/java/com/allan/mapper/OrderMapper.java
package com.allan.mapper;

import com.allan.dto.*;
import com.allan.model.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

// import com.allan.dto.AddressSummaryDTO;
// import com.allan.dto.OrderDTO;
// import com.allan.dto.OrderItemDTO;
// import com.allan.dto.ProductSummaryDTO;
// import com.allan.dto.UserSummaryDTO;
// import com.allan.model.Address;
// import com.allan.model.Order;
// import com.allan.model.OrderItem;

@Component
public class OrderMapper {

    // ✅ converts Order entity → OrderDTO
    public OrderDTO toOrderDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderId(order.getOrderId());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setTotalMrpPrice(order.getTotalMrpPrice());
        dto.setTotalSellingPrice(order.getTotalSellingPrice());
        dto.setDiscount(order.getDiscount());
        dto.setTotalItem(order.getTotalItem());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setOrderDate(order.getOrderDate());
        dto.setDeliverDate(order.getDeliverDate());
        dto.setSellerId(order.getSellerId());

        // ✅ map only needed user fields
        if (order.getUser() != null) {
            dto.setUser(toUserSummaryDTO(order.getUser()));
        }

        // ✅ map only needed address fields
        if (order.getShippingAddress() != null) {
            dto.setShippingAddress(toAddressSummaryDTO(order.getShippingAddress()));
        }

        // ✅ map order items
        if (order.getOrderItems() != null) {
            dto.setOrderItems(order.getOrderItems().stream()
                    .map(this::toOrderItemDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public List<OrderDTO> toOrderDTOList(List<Order> orders) {
        return orders.stream()
                .map(this::toOrderDTO)
                .collect(Collectors.toList());
    }

    private UserSummaryDTO toUserSummaryDTO(User user) {
        UserSummaryDTO dto = new UserSummaryDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setMobile(user.getMobile());
        return dto;
    }

    private AddressSummaryDTO toAddressSummaryDTO(Address address) {
        AddressSummaryDTO dto = new AddressSummaryDTO();
        dto.setId(address.getId());
        dto.setName(address.getName());
        dto.setAddress(address.getAddress());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setPinCode(address.getPinCode());
        dto.setMobile(address.getMobile());
        return dto;
    }

    private OrderItemDTO toOrderItemDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setQuantity(item.getQuantity());
        dto.setMrpPrice(item.getMrpPrice());
        dto.setSellingPrice(item.getSellingPrice());
        dto.setSize(item.getSize());

        if (item.getProduct() != null) {
            dto.setProduct(toProductSummaryDTO(item.getProduct()));
        }

        return dto;
    }

    private ProductSummaryDTO toProductSummaryDTO(Product product) {
        ProductSummaryDTO dto = new ProductSummaryDTO();
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setMrpPrice(product.getMrpPrice());
        dto.setSellingPrice(product.getSellingPrice());
        dto.setDiscountPercent(product.getDiscountPercent());
        dto.setImages(product.getImages());
        dto.setColor(product.getColor());
        return dto;
    }
}