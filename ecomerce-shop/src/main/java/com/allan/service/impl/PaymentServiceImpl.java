package com.allan.service.impl;

import java.util.Set;

import jakarta.annotation.PostConstruct;

// import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.allan.domain.PaymentOrderStatus;
import com.allan.domain.PaymentStatus;
import com.allan.domain.SupportedCurrency;
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

    // Config-driven, not hardcoded — changing the platform's default
    // currency later is a properties change, not a code change. Defaults
    // to UGX if the property is omitted, so nothing breaks if you never
    // set it explicitly.
    @Value("${app.payment.default-currency:UGX}")
    private String defaultCurrency;

    // Was hardcoded to http://localhost:3000/... — harmless in local dev,
    // silently broken in any real deployment (customers would get bounced
    // to a URL that doesn't exist after paying). Same @Value pattern
    // AdminController already uses for app.frontend.verify-admin-url.
    @Value("${app.frontend.payment-success-url}")
    private String paymentSuccessUrlBase;

    @Value("${app.frontend.payment-cancel-url}")
    private String paymentCancelUrl;

    // Resolved once at startup, not re-parsed on every checkout request —
    // see validateConfiguredCurrency() below for why this matters.
    private SupportedCurrency resolvedCurrency;

    /**
     * Fails application startup if app.payment.default-currency doesn't
     * match a SupportedCurrency constant, instead of discovering the typo
     * when a real customer hits checkout and gets an unhandled
     * IllegalArgumentException mid-payment. A misconfigured currency is a
     * deployment-time problem, not a runtime-per-request one — this makes
     * sure it's caught at the point where it's cheap to fix (a failed
     * deploy) rather than the point where it's expensive (a broken
     * checkout in production, possibly mid-incident).
     */
    @PostConstruct
    void validateConfiguredCurrency() {
        try {
            resolvedCurrency = SupportedCurrency.valueOf(defaultCurrency.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "app.payment.default-currency is set to '" + defaultCurrency
                            + "', which isn't a supported currency. Valid values: "
                            + java.util.Arrays.toString(SupportedCurrency.values()), ex);
        }
    }


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

@Override
    public String createStripePaymentLink(User user, Long amount, Long orderId) throws StripeException {
        // Fail with a clear, specific error before ever calling Stripe's API,
        // rather than letting Stripe reject a null/zero/negative amount with
        // a less obvious error, or — worse — silently accepting a bad value.
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be a positive value, got: " + amount);
        }

        Stripe.apiKey = stripeSecretKey;

        // Uses the currency resolved once at startup (see
        // validateConfiguredCurrency) rather than re-parsing the config
        // string on every checkout — cheaper, and guarantees this method
        // can never throw on a bad currency config, since that failure
        // already happened at boot.
        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(paymentSuccessUrlBase + orderId)
                .setCancelUrl(paymentCancelUrl)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(resolvedCurrency.getStripeCode())
                                .setUnitAmount(resolvedCurrency.toStripeUnitAmount(amount))
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

