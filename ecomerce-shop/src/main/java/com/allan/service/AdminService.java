package com.allan.service;

import java.util.List;

import com.allan.domain.AccountStatus;
import com.allan.model.*;
import com.allan.request.LoginRequest;
import com.allan.response.AuthResponse;
import com.allan.dto.OrderDTO;

public interface AdminService {

    // ── Auth ──────────────────────────────────────────
    Admin createAdmin(Admin admin) throws Exception;
    Admin verifyAdminEmail(String email, String otp) throws Exception;
    AuthResponse loginAdmin(LoginRequest loginRequest) throws Exception;

    // ── Sellers ───────────────────────────────────────
    List<Seller> getAllSellers(AccountStatus status) throws Exception;
    Seller updateSellerStatus(Long id, AccountStatus status) throws Exception;
    void deleteSeller(Long id) throws Exception;
    SellerReport getSellerReport(Long sellerId) throws Exception;

    // ── Orders ────────────────────────────────────────
    List<OrderDTO> getAllOrders(String status) throws Exception;
    OrderDTO updateOrderStatus(Long id, String status) throws Exception;

    // ── Products ──────────────────────────────────────
    List<Product> getAllProducts(String category) throws Exception;
    void deleteProduct(Long id) throws Exception;

    // ── Deals ─────────────────────────────────────────
    List<Deal> getAllDeals() throws Exception;
    Deal createDeal(Deal deal) throws Exception;
    Deal updateDeal(Long id, Deal deal) throws Exception;
    void deleteDeal(Long id) throws Exception;

    // ── Coupons ───────────────────────────────────────
    List<Coupon> getAllCoupons() throws Exception;
    Coupon createCoupon(Coupon coupon) throws Exception;
    void deleteCoupon(Long id) throws Exception;
}