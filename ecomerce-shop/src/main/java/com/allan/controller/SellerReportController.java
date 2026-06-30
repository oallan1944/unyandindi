package com.allan.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.allan.model.Seller;
import com.allan.model.SellerReport;
import com.allan.service.SellerReportService;
import com.allan.service.SellerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller/report")
public class SellerReportController {

    private final SellerReportService sellerReportService;
    private final SellerService sellerService;

    /** Seller fetches their own report using their JWT */
    @GetMapping
    public ResponseEntity<SellerReport> getSellerReport(
            @RequestHeader("Authorization") String jwt) throws Exception {
        Seller seller = sellerService.getSellerProfile(jwt);
        SellerReport report = sellerReportService.getSellerReport(seller);
        return ResponseEntity.ok(report);
    }
}
