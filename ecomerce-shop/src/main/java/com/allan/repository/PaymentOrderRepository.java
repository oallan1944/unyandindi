package com.allan.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.allan.model.PaymentOrder;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

    PaymentOrder findByPaymentLinkId(String paymentId);

}
