package com.allan.service.impl;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.allan.domain.OrderStatus;
import com.allan.domain.PaymentStatus;
import com.allan.dto.OrderDTO;
import com.allan.mapper.OrderMapper;
import com.allan.model.Address;
import com.allan.model.Cart;
import com.allan.model.CartItem;
import com.allan.model.Order;
import com.allan.model.OrderItem;
import com.allan.model.User;
import com.allan.repository.AddressRepository;
import com.allan.repository.OrderItemRepository;
import com.allan.repository.OrderRepository;
import com.allan.service.OrderService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;

    /**
     * Write operation — full @Transactional to guarantee atomicity across
     * multiple saves (address, orders, order items). If any save fails, the
     * entire unit rolls back cleanly.
     */
    
 // ── Replace only the createOrder method — everything else unchanged ──

    @Override
    @Transactional
    public Set<Order> createOrder(User user, Address shippingAddress,
                                Cart cart) {

    // ✅ fix: original called user.getAddresses().contains() and .add()
    // which triggers a lazy load of the addresses collection.
    // Since addresses is now FetchType.LAZY, this fires an extra SQL
    // query loading all addresses just to do a contains check.
    // The address is saved directly here instead — the User→Address
    // relationship is maintained by the Address row itself, not by
    // loading the full collection into memory.
    Address address = addressRepository.save(shippingAddress);

    Map<Long, List<CartItem>> itemsBySeller = cart.getCartItems()
            .stream()
            .collect(Collectors.groupingBy(
                    item -> item.getProduct().getSeller().getId()));

    Set<Order> orders = new HashSet<>();

    for (Map.Entry<Long, List<CartItem>> entry :
            itemsBySeller.entrySet()) {
        Long sellerId = entry.getKey();
        List<CartItem> items = entry.getValue();

        int totalOrderPrice = items.stream()
                .mapToInt(CartItem::getSellingPrice)
                .sum();

        int totalItem = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        Order createdOrder = new Order();
        createdOrder.setUser(user);
        createdOrder.setSellerId(sellerId);
        createdOrder.setTotalMrpPrice(totalOrderPrice);
        createdOrder.setTotalSellingPrice(totalOrderPrice);
        createdOrder.setTotalItem(totalItem);
        createdOrder.setShippingAddress(address);
        createdOrder.setOrderStatus(OrderStatus.PENDING);
        createdOrder.getPaymentDetails()
                .setStatus(PaymentStatus.PENDING);

        Order savedOrder = orderRepository.save(createdOrder);
        orders.add(savedOrder);

        for (CartItem item : items) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setMrpPrice(item.getMrpPrice());
            orderItem.setProduct(item.getProduct());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setSize(item.getSize());
            orderItem.setUserId(item.getUserId());
            orderItem.setSellingPrice(item.getSellingPrice());

            savedOrder.getOrderItems().add(orderItem);
            orderItemRepository.save(orderItem);
        }
    }

    return orders;
}

    /**
     * Read — readOnly hint allows Hibernate to skip dirty checking on all
     * loaded entities, reduces flush overhead, and signals the connection pool
     * that no write lock is needed.
     */
    @Override
    @Transactional(readOnly = true)
    public OrderDTO findOrderById(long id) throws Exception {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new Exception("Order not found with id: " + id));
        return orderMapper.toOrderDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> userOrderHistory(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orderMapper.toOrderDTOList(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> sellersOrder(Long sellerId) {
        List<Order> orders = orderRepository.findBySellerId(sellerId);
        return orderMapper.toOrderDTOList(orders);
    }

    /**
     * Write — status update must be transactional to guarantee the save is
     * committed even if a downstream caller fails between the update and
     * its own flush.
     */
    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, OrderStatus orderStatus) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new Exception("Order not found with id: " + orderId));
        order.setOrderStatus(orderStatus);
        Order saved = orderRepository.save(order);
        return orderMapper.toOrderDTO(saved);
    }

    @Override
    @Transactional
    public OrderDTO cancelOrder(Long orderId, User user) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new Exception("Order not found with id: " + orderId));

        if (!user.getId().equals(order.getUser().getId())) {
            throw new Exception("You do not have access to this order");
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        return orderMapper.toOrderDTO(saved);
    }

    /**
     * Read — narrow lookup, no write side effect.
     */
    @Override
    @Transactional(readOnly = true)
    public OrderItem getOrderItemById(Long id) throws Exception {
        return orderItemRepository.findById(id)
                .orElseThrow(() -> new Exception("Order item not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> findAllOrders(String status) throws Exception {
        List<Order> orders;

        if (status != null && !status.isBlank()) {
            OrderStatus orderStatus;
            try {
                orderStatus = OrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new Exception("Invalid order status: " + status
                        + ". Valid values: " + java.util.Arrays.toString(OrderStatus.values()));
            }
            orders = orderRepository.findByOrderStatus(orderStatus);
        } else {
            orders = orderRepository.findAll();
        }

        return orderMapper.toOrderDTOList(orders); // ✅ uses the real mapper, not a nonexistent method
}


}







