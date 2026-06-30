package com.allan.service;

import com.allan.model.Seller;
import com.allan.model.SellerReport;

public interface SellerReportService {

    SellerReport getSellerReport(Seller seller);

    SellerReport updateSellerReport(SellerReport sellerReport);



}
