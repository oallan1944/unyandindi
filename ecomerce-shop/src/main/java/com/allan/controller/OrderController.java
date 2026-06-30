package com.allan.controller;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.allan.domain.PaymentMethod;
import com.allan.dto.OrderDTO;
import com.allan.model.*;
import com.allan.response.PaymentLinkResponse;
import com.allan.service.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final CartService cartService;
    private final SellerService sellerService;
    private final SellerReportService sellerReportService;
    private final PaymentService paymentService;

    /**
     * POST /api/orders
     * Creates orders from the user's cart, initiates a Stripe payment session,
     * and returns the checkout URL.
     */
    @PostMapping
    public ResponseEntity<PaymentLinkResponse> createOrder(
            @RequestBody Address shippingAddress,
            @RequestParam PaymentMethod paymentMethod,
            @RequestHeader("Authorization") String jwt) throws Exception {

        User user = userService.findUserByJwtToken(jwt);
        Cart cart = cartService.findUserCart(user);
        Set<Order> orders = orderService.createOrder(user, shippingAddress, cart);

        PaymentOrder paymentOrder = paymentService.createOrder(user, orders);

        String paymentUrl = paymentService.createStripePaymentLink(
                user,
                paymentOrder.getAmount(),
                paymentOrder.getId());

        PaymentLinkResponse response = new PaymentLinkResponse();
        response.setPayment_link_url(paymentUrl);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/orders/user
     * Returns the authenticated user's full order history.
     */
    @GetMapping("/user")
    public ResponseEntity<List<OrderDTO>> getUserOrderHistory(
            @RequestHeader("Authorization") String jwt) throws Exception {

        User user = userService.findUserByJwtToken(jwt);
        List<OrderDTO> orders = orderService.userOrderHistory(user.getId());
        return ResponseEntity.ok(orders);
    }

    /**
     * GET /api/orders/{orderId}
     * Returns a single order by ID. Auth is verified; ownership is not
     * enforced here (admin/seller use cases may also call this).
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrderById(
            @PathVariable long orderId,
            @RequestHeader("Authorization") String jwt) throws Exception {

        userService.findUserByJwtToken(jwt); // auth guard — validates token
        OrderDTO order = orderService.findOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    /**
     * GET /api/orders/item/{orderItemId}
     * Returns a single order item. Useful for review/return flows.
     */
    @GetMapping("/item/{orderItemId}")
    public ResponseEntity<OrderItem> getOrderItemById(
            @PathVariable Long orderItemId,
            @RequestHeader("Authorization") String jwt) throws Exception {

        userService.findUserByJwtToken(jwt); // auth guard
        OrderItem orderItem = orderService.getOrderItemById(orderItemId);
        return ResponseEntity.ok(orderItem);
    }

    /**
     * PUT /api/orders/{orderId}/cancel
     * Cancels an order, updates seller cancellation report atomically.
     */
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String jwt) throws Exception {

        User user = userService.findUserByJwtToken(jwt);
        OrderDTO cancelledOrder = orderService.cancelOrder(orderId, user);

        // Update seller report — fetch seller by the sellerId stored on the DTO
        // Note: cancelOrder() in the service already persists CANCELLED status,
        // so the report update below is the only remaining side effect here.
        Order rawOrder = new Order();
        rawOrder.setSellerId(cancelledOrder.getSellerId());
        rawOrder.setTotalSellingPrice(cancelledOrder.getTotalSellingPrice());

        Seller seller = sellerService.getSellerbyId(cancelledOrder.getSellerId());
        SellerReport report = sellerReportService.getSellerReport(seller);
        report.setCanceledOrders(report.getCanceledOrders() + 1);
        report.setTotalRefunds(report.getTotalRefunds() + cancelledOrder.getTotalSellingPrice());
        sellerReportService.updateSellerReport(report);

        return ResponseEntity.ok(cancelledOrder);
    }
}







// package com.allan.controller;

// import java.util.List;
// import java.util.Set;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestHeader;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;

// import com.allan.domain.PaymentMethod;
// import com.allan.model.Address;
// import com.allan.model.Cart;
// import com.allan.model.Order;
// import com.allan.model.OrderItem;
// import com.allan.model.PaymentOrder;
// import com.allan.model.Seller;
// import com.allan.model.SellerReport;
// import com.allan.model.User;
// // import com.allan.repository.PaymentOrderRepository;
// import com.allan.response.PaymentLinkResponse;
// import com.allan.service.CartService;
// import com.allan.service.OrderService;
// import com.allan.service.PaymentService;
// import com.allan.service.SellerReportService;
// import com.allan.service.SellerService;
// import com.allan.service.UserService;
// // import com.razorpay.PaymentLink;

// import lombok.RequiredArgsConstructor;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PutMapping;

// @RestController
// @RequiredArgsConstructor
// @RequestMapping("/api/orders")
// public class OrderController {

//     private final OrderService orderService;
//     private final UserService userService;
//     private final CartService cartService;
//     private final SellerService sellerService;
//     private final SellerReportService sellerReportService;
//     private final PaymentService paymentService;
//     // private final PaymentOrderRepository paymentOrderRepository;

//     /**
//      * Post/api/oders
//      * Creates orders from the user's cart, initiates strope payement
//      * returns the checkout url
//      */
//     @PostMapping()
//     public ResponseEntity<PaymentLinkResponse> createOrderHandler(
//             @RequestBody Address shippingAddress,
//             @RequestParam PaymentMethod paymentMethod,
//             @RequestHeader("Authorization") String jwt) throws Exception {
//         User user = userService.findUserByJwtToken(jwt);
//         Cart cart = cartService.findUserCart(user);
//         Set<Order> orders = orderService.createOrder(user, shippingAddress, cart);

//         PaymentOrder paymentOrder = paymentService.createOrder(user, orders);

//         PaymentLinkResponse res = new PaymentLinkResponse();

//         // if (paymentMethod.equals(PaymentMethod.RAZORPAY)) {
//         // PaymentLink payment = paymentService.createRazarPayPaymentLink(
//         // user,
//         // paymentOrder.getAmount(),
//         // paymentOrder.getId());

//         // String paymentUrl = payment.get("short_url");
//         // String paymentUrlId = payment.get("id");

//         // res.setPayment_link_url(paymentUrl);

//         // paymentOrder.setPaymentLinkId(paymentUrlId);
//         // paymentOrderRepository.save(paymentOrder);
//         // } else {
//         String paymentUrl = paymentService.createStripePaymentLink(
//                 user,
//                 paymentOrder.getAmount(),
//                 paymentOrder.getId());
//         res.setPayment_link_url(paymentUrl);
//         // }

//         return new ResponseEntity<>(res, HttpStatus.OK);
//     }

//     @GetMapping("/user")
//     public ResponseEntity<List<Order>> userOrderHistoryHandler(
//             @RequestHeader("Authorization") String jwt) throws Exception {

//         User user = userService.findUserByJwtToken(jwt);
//         List<Order> orders = orderService.userOrderHistory(user.getId());
//         return new ResponseEntity<>(orders, HttpStatus.ACCEPTED);
//     }

//     @GetMapping("/{orderId}")
//     public ResponseEntity<Order> getOrderById(@PathVariable long orderId,
//             @RequestHeader("Authorization") String jwt) throws Exception {
//         User user = userService.findUserByJwtToken(jwt);
//         Order orders = orderService.findOrderById(orderId);

//         return new ResponseEntity<>(orders, HttpStatus.ACCEPTED);
//     }

//     @GetMapping("/item/{orderItemId}")
//     public ResponseEntity<OrderItem> getOrderItemById(
//             @PathVariable Long orderItemId,
//             @RequestHeader("Authorization") String jwt) throws Exception {

//         // User user = userService.findUserByJwtToken(jwt);
//         OrderItem orderItem = orderService.getOrderItemById(orderItemId);
//         return new ResponseEntity<>(orderItem, HttpStatus.ACCEPTED);
//     }

//     @PutMapping("/{orderId}/cancel")
//     public ResponseEntity<Order> cancelOrder(
//             @PathVariable Long orderId,
//             @RequestHeader("Authorization") String jwt) throws Exception {

//         User user = userService.findUserByJwtToken(jwt);
//         Order order = orderService.cancelOrder(orderId, user);

//         Seller seller = sellerService.getSellerbyId(order.getSellerId());
//         SellerReport report = sellerReportService.getSellerReport(seller);

//         report.setCanceledOrders(report.getCanceledOrders() + 1);
//         report.setTotalRefunds(report.getTotalRefunds() + order.getTotalSellingPrice());
//         sellerReportService.updateSellerReport(report);

//         return ResponseEntity.ok(order);
//     }

// }
