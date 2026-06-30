package com.allan.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.allan.model.Order;
import com.allan.model.PaymentOrder;
import com.allan.model.Seller;
import com.allan.model.SellerReport;
import com.allan.model.User;
import com.allan.response.ApiResponse;
import com.allan.response.PaymentLinkResponse;
import com.allan.service.PaymentService;
import com.allan.service.SellerReportService;
import com.allan.service.SellerService;
import com.allan.service.TransactionService;
import com.allan.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;
    private final SellerService sellerService;
   // private final OrderService orderService;
    private final SellerReportService sellerReportService;
    private final TransactionService transactionService;

    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse> paymentSuccessHandler(
            @PathVariable String paymentId,
            @RequestParam String paymentLinkId,
            @RequestHeader("Authorization") String jwt) throws Exception {
        User user = userService.findUserByJwtToken(jwt);

        PaymentLinkResponse paymentResponse;

        PaymentOrder paymentOrder = paymentService
                .getPaymentOrderByPaymentId(paymentLinkId);

        boolean paymentSuccess = paymentService.proceedPaymentOrder(
                paymentOrder,
                paymentId,
                paymentLinkId);

        if (paymentSuccess) {
            for (Order order : paymentOrder.getOrders()) {
                transactionService.createTransaction(order);
                Seller seller = sellerService.getSellerbyId(order.getSellerId());
                SellerReport report = sellerReportService.getSellerReport(seller);
                report.setTotalOrders(report.getTotalOrders() + 1);
                report.setTotalEarnings(report.getTotalEarnings() + order.getTotalSellingPrice());
                report.setTotalSales(report.getTotalSales() + order.getOrderItems().size());
                sellerReportService.updateSellerReport(report);
            }
        }

        ApiResponse res = new ApiResponse();
        res.setMessage("Payment Successful");
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

}
