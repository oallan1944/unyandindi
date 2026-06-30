package com.allan.service;

import java.util.List;
import java.util.Set;

import com.allan.domain.OrderStatus;
import com.allan.dto.OrderDTO;
import com.allan.model.Address;
import com.allan.model.Cart;
import com.allan.model.Order;
import com.allan.model.OrderItem;
import com.allan.model.User;


public interface OrderService {

    Set<Order> createOrder(User user, Address shippingAddress, Cart cart);

    OrderDTO findOrderById(long id) throws Exception;

    List<OrderDTO> userOrderHistory(Long userId);

    List<OrderDTO> sellersOrder(Long sellerId);

    OrderDTO updateOrderStatus(Long orderId, OrderStatus orderStatus) throws Exception;

    OrderDTO cancelOrder(Long orderId, User user) throws Exception;

    OrderItem getOrderItemById(Long id) throws Exception;

    List <OrderDTO> findAllOrders(String status) throws Exception;
}



// package com.allan.service;

// import java.util.List;
// import java.util.Set;

// import com.allan.domain.OrderStatus;
// import com.allan.model.Address;
// import com.allan.model.Cart;
// import com.allan.model.Order;
// import com.allan.model.OrderItem;
// import com.allan.model.User;

// public interface OrderService {
//     Set<Order> createOrder(User user, Address shippingAddress, Cart cart);

//     Order findOrderById(long id) throws Exception;

//     List<Order> userOrderHistory(Long userId);

//     List<Order> sellersOrder(Long sellerId);

//     Order updateOrderStatus(Long orderId, OrderStatus OrderStatus) throws Exception;

//     Order cancelOrder(Long orderId, User user) throws Exception;

//     OrderItem getOrderItemById(Long id) throws Exception;
// }
