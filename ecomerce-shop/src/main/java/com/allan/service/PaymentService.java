package com.allan.service;

import java.util.Set;

import com.allan.model.Order;
import com.allan.model.PaymentOrder;
import com.allan.model.User;
// import com.razorpay.PaymentLink;
import com.razorpay.RazorpayException;
import com.stripe.exception.StripeException;

public interface PaymentService {

        PaymentOrder createOrder(User user, Set<Order> orders);

        PaymentOrder getPaymentOrderById(Long orderId) throws Exception;

        PaymentOrder getPaymentOrderByPaymentId(String orderId) throws Exception;

        Boolean proceedPaymentOrder(PaymentOrder paymentOrder,
                        String paymentId,
                        String paymentLinkId) throws RazorpayException, StripeException;

        // PaymentLink createRazarPayPaymentLink(User user,
        //                 Long amount,
        //                 Long orderId) throws RazorpayException;

        String createStripePaymentLink(User user,
                        Long amount, Long orderId) throws StripeException;

}
