package com.allan.service.impl;

import java.util.Set;

// import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.allan.domain.PaymentOrderStatus;
import com.allan.domain.PaymentStatus;
import com.allan.model.Order;
import com.allan.model.PaymentOrder;
import com.allan.model.User;
import com.allan.repository.OrderRepository;
import com.allan.repository.PaymentOrderRepository;
import com.allan.service.PaymentService;
// import com.razorpay.Payment;
// import com.razorpay.PaymentLink;
// import com.razorpay.RazorpayClient;
// import com.razorpay.RazorpayException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final OrderRepository orderRepository;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    // @Value("${razorpay.api.key}")
    // private String apiKey;

    // @Value("${razorpay.api.secret}")
    // private String apiSecret;

    @Override
    public PaymentOrder createOrder(User user, Set<Order> orders) {

        Long amount = orders.stream().mapToLong(Order::getTotalSellingPrice).sum();

        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setAmount(amount);
        paymentOrder.setUser(user);
        paymentOrder.setOrders(orders);
        return paymentOrderRepository.save(paymentOrder);
    }

    @Override
    public PaymentOrder getPaymentOrderById(Long orderId) throws Exception {
        return paymentOrderRepository.findById(orderId).orElseThrow(() -> new Exception("Payment order not found..."));
    }

    @Override
    public PaymentOrder getPaymentOrderByPaymentId(String orderId) throws Exception {

        PaymentOrder paymentOrder = paymentOrderRepository.findByPaymentLinkId(orderId);

        if (paymentOrder == null) {
            throw new Exception("Payment Order not found...");
        }

        return paymentOrder;
    }

    @Override
    public Boolean proceedPaymentOrder(PaymentOrder paymentOrder, String paymentIntentId, String unusedLinkId)
            throws StripeException {

        if (paymentOrder.getStatus().equals(PaymentOrderStatus.PENDING)) {
            // Fetch the PaymentIntent from Stripe
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            String status = paymentIntent.getStatus();

            if ("succeeded".equals(status)) {
                Set<Order> orders = paymentOrder.getOrders();
                for (Order order : orders) {
                    order.setPaymentStatus(PaymentStatus.COMPLETED);
                    orderRepository.save(order);
                }

                paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
                paymentOrderRepository.save(paymentOrder);
                return true;
            }

            // If payment not succeeded, mark as failed
            paymentOrder.setStatus(PaymentOrderStatus.FAILED);
            paymentOrderRepository.save(paymentOrder);
            return false;
        }
        return false;
    }

    // @Override
    // public Boolean proceedPaymentOrder(PaymentOrder paymentOrder, String
    // paymentId, String paymentLinkId)
    // throws RazorpayException {

    // if (paymentOrder.getStatus().equals(PaymentOrderStatus.PENDING)) {
    // RazorpayClient razorpay = new RazorpayClient(apiKey, apiSecret);

    // Payment payment = razorpay.payments.fetch(paymentId);

    // String status = payment.get("status");

    // if (status.equals("captured")) {
    // Set<Order> orders = paymentOrder.getOrders();
    // for (Order order : orders) {
    // order.setPaymentStatus(PaymentStatus.COMPLETED);
    // orderRepository.save(order);
    // }

    // paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
    // paymentOrderRepository.save(paymentOrder);
    // return true;
    // }
    // paymentOrder.setStatus(PaymentOrderStatus.FAILED);
    // paymentOrderRepository.save(paymentOrder);
    // return false;
    // }
    // return false;
    // }

    // @Override
    // public PaymentLink createRazarPayPaymentLink(User user, Long amount, Long
    // orderId) throws RazorpayException {

    // amount = amount * 100;

    // try {
    // RazorpayClient razorpay = new RazorpayClient(apiKey, apiSecret);

    // JSONObject paymentLinkRequest = new JSONObject();
    // paymentLinkRequest.put("amount", amount);
    // paymentLinkRequest.put("currency", "USD");

    // JSONObject customer = new JSONObject();
    // customer.put("name", user.getFullName());
    // customer.put("email", user.getEmail());
    // paymentLinkRequest.put("customer", customer);

    // JSONObject notify = new JSONObject();
    // notify.put("email", true);
    // paymentLinkRequest.put("notify", notify);

    // paymentLinkRequest.put("callback_url",
    // "http://localhost:3000/payment-success/" + orderId);
    // paymentLinkRequest.put("callback_method", "get");

    // PaymentLink paymentLink = razorpay.paymentLink.create(paymentLinkRequest);

    // String paymentLinkUrl = paymentLink.get("short_url");
    // String paymentLinkId = paymentLink.get("id");

    // return paymentLink;

    // } catch (Exception e) {
    // System.out.println(e.getMessage());
    // throw new RazorpayException(e.getMessage());
    // }

    // }

    // repalce with ai code

    @Override
    public String createStripePaymentLink(User user, Long amount, Long orderId) throws StripeException {
        Stripe.apiKey = stripeSecretKey;

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:3000/payment-success/" + orderId)
                .setCancelUrl("http://localhost:3000/payment-cancele/")
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmount(amount * 100)
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData
                                        .builder().setName("Huru payment")
                                        .build())
                                .build())
                        .build())
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }

}

// @Override
// public String createStripePaymentLink(User user, Long amount, Long orderId) {
// try {
// // Create a Product (or reuse a static one if you'd prefer)
// ProductCreateParams productParams = ProductCreateParams.builder()
// .setName("Order #" + orderId)
// .build();
// Product product = Product.create(productParams);

// // Create a Price for the product
// PriceCreateParams priceParams = PriceCreateParams.builder()
// .setProduct(product.getId())
// .setUnitAmount(amount * 100) // Stripe expects amount in cents
// .setCurrency("usd")
// .build();
// Price price = Price.create(priceParams);

// // Create the Payment Link with redirect callback
// PaymentLinkCreateParams linkParams = PaymentLinkCreateParams.builder()
// .addLineItem(
// PaymentLinkCreateParams.LineItem.builder()
// .setPrice(price.getId())
// .setQuantity(1L)
// .build()
// )
// .setAfterCompletion(
// PaymentLinkCreateParams.AfterCompletion.builder()
// .setType(PaymentLinkCreateParams.AfterCompletion.Type.REDIRECT)
// .setRedirect(
// PaymentLinkCreateParams.AfterCompletion.Redirect.builder()
// .setUrl("http://localhost:3000/payment-success/" + orderId)
// .build()
// )
// .build()
// )
// .putMetadata("orderId", orderId.toString())
// .build();

// String paymentLink = PaymentLink.create(linkParams);

// // (Optional) Log or return payment link URL and ID
// String paymentLinkUrl = paymentLink.getUrl();
// String paymentLinkId = paymentLink.getId();

// return paymentLink;

// } catch (StripeException e) {
// System.out.println(e.getMessage());
// throw new RuntimeException("Failed to create Stripe payment link", e);
// }
// }