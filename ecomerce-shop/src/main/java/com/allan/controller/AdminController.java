package com.allan.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import com.allan.domain.AccountStatus;
import com.allan.dto.OrderDTO;
import com.allan.dto.SellerProfileDTO;
import com.allan.dto.SellerReportDTO;
import com.allan.dto.SellerSummaryDTO;
import com.allan.mapper.OrderMapper;
import com.allan.mapper.SellerMapper;
import com.allan.model.*;
import com.allan.repository.VerificationCodeRepository;
import com.allan.request.LoginRequest;
import com.allan.response.AuthResponse;
import com.allan.service.AdminService;
import com.allan.service.AuthService;
import com.allan.service.EmailService;
import com.allan.utils.OtpUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Admin-facing REST controller.
 *
 * All endpoints here are restricted to ROLE_ADMIN via SecurityConfig.
 * Public endpoints (create, login, verify) are explicitly permitted there.
 *
 * Response shaping rule:
 *  - Entities with sensitive fields (password, bank details, lazy collections)
 *    are NEVER returned directly — always passed through a Mapper to a DTO.
 *  - Simple entities with no sensitive/lazy fields (Deal, Coupon) may be
 *    returned directly until their own DTOs are introduced.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final AuthService authService;
    private final EmailService emailService;
    private final VerificationCodeRepository verificationCodeRepository;
    private final OrderMapper orderMapper;
    private final SellerMapper sellerMapper;

   // $(check) VERIFY_FRONTEND_URL is injected from application.properties

   @Value("${app.frontend.verify-admin-url}")
   private String verifyAdminUrl;

    // ═════════════════════════════════════════════════════════════
    // ADMIN ACCOUNT — CREATE / VERIFY / LOGIN
    // ═════════════════════════════════════════════════════════════

    /**
     * Create a new admin account and dispatch an OTP verification email.
     * Public endpoint — must be permitted in SecurityConfig.
     */
    @PostMapping("/create")
    public ResponseEntity<Admin> createAdmin(@RequestBody Admin admin) throws Exception {
        Admin savedAdmin = adminService.createAdmin(admin);

        String otp = OtpUtil.generateOtp();
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setOtp(otp);
        verificationCode.setEmail(savedAdmin.getEmail());
        verificationCodeRepository.save(verificationCode);

       emailService.sendVerificationOtpEmail(
            savedAdmin.getEmail(),
            otp,
            "Huru Bazar Admin Account Verification",
            "Welcome to Huru Bazar Admin Portal. Verify your admin account using this link: "
                    + verifyAdminUrl  // ✅ injected field, not a hardcoded constant
        );

        log.info("Admin account created: {}", savedAdmin.getEmail());
        return new ResponseEntity<>(savedAdmin, HttpStatus.CREATED);
    
    }


    /**
     * Verify an admin's email using a one-time OTP.
     * Public endpoint — must be permitted in SecurityConfig.
     */
    @PatchMapping("/verify/{otp}")
    public ResponseEntity<Admin> verifyAdminEmail(@PathVariable String otp) throws Exception {
        if (otp == null || otp.isBlank()) {
            log.warn("Admin verification attempted with blank OTP");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        VerificationCode verificationCode = verificationCodeRepository.findByOtp(otp);
        if (verificationCode == null) {
            log.warn("Admin verification failed — OTP not found: {}", otp);
            throw new Exception("Invalid or expired OTP. Please request a new verification email.");
        }

        Admin admin = adminService.verifyAdminEmail(verificationCode.getEmail(), otp);
        log.info("Admin email verified: {}", admin.getEmail());
        return ResponseEntity.ok(admin);
    }

    /**
     * Admin login using email + OTP.
     * Public endpoint — must be permitted in SecurityConfig.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginAdmin(@RequestBody VerificationCode req) throws Exception {
        if (req.getEmail() == null || req.getEmail().isBlank()
                || req.getOtp() == null || req.getOtp().isBlank()) {
            log.warn("Admin login attempted with missing email or OTP");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(req.getEmail());
        loginRequest.setOtp(req.getOtp());

        AuthResponse authResponse = authService.signing(loginRequest);
        log.info("Admin login successful: {}", req.getEmail());
        return ResponseEntity.ok(authResponse);
    }

    // ═════════════════════════════════════════════════════════════
    // SELLER MANAGEMENT
    // ═════════════════════════════════════════════════════════════

    /** List sellers, optionally filtered by account status. No sensitive fields exposed. */
    @GetMapping("/sellers")
    public ResponseEntity<List<SellerSummaryDTO>> getAllSellers(
            @RequestParam(required = false) AccountStatus status) throws Exception {
        List<Seller> sellers = adminService.getAllSellers(status);
        return ResponseEntity.ok(sellerMapper.toSummaryDTOList(sellers));
    }

    /** Update a seller's account status (e.g. ACTIVE, SUSPENDED). */
    @PatchMapping("/sellers/{id}/status/{status}")
    public ResponseEntity<SellerProfileDTO> updateSellerStatus(
            @PathVariable Long id,
            @PathVariable AccountStatus status) throws Exception {
        Seller updatedSeller = adminService.updateSellerStatus(id, status);
        log.info("Seller {} status updated to {}", id, status);
        return ResponseEntity.ok(sellerMapper.toProfileDTO(updatedSeller));
    }

    /** Permanently delete a seller account. */
    @DeleteMapping("/sellers/{id}")
    public ResponseEntity<Void> deleteSeller(@PathVariable Long id) throws Exception {
        adminService.deleteSeller(id);
        log.info("Seller {} deleted by admin", id);
        return ResponseEntity.noContent().build();
    }

    /** Generate a performance report for a single seller. */
    @GetMapping("/sellers/{id}/report")
    public ResponseEntity<SellerReportDTO> getSellerReport(@PathVariable Long id) throws Exception {
        SellerReport report = adminService.getSellerReport(id);
        return ResponseEntity.ok(sellerMapper.toReportDTO(report));
    }

    // ═════════════════════════════════════════════════════════════
    // ORDER MANAGEMENT
    // ═════════════════════════════════════════════════════════════

    // AdminController.java
@GetMapping("/orders")
public ResponseEntity<List<OrderDTO>> getAllOrders(
        @RequestParam(required = false) String status) throws Exception {
    return ResponseEntity.ok(adminService.getAllOrders(status));
}

    /** Update an order's status. */
    @PatchMapping("/orders/{id}/status/{status}")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long id,
            @PathVariable String status) throws Exception {
        OrderDTO updated = adminService.updateOrderStatus(id, status);
        log.info("Order {} status updated to {}", id, status);
        return ResponseEntity.ok(updated);
    }

    // ═════════════════════════════════════════════════════════════
    // PRODUCT MANAGEMENT
    // ═════════════════════════════════════════════════════════════

    /** List products, optionally filtered by category. */
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts(
            @RequestParam(required = false) String category) throws Exception {
        List<Product> products = adminService.getAllProducts(category);
        return ResponseEntity.ok(products);
    }


    /** Add a product to the Electronics home section. */
    @PostMapping("/products/electric-category")
    public ResponseEntity<Product> addProductToElectricCategory(
            @RequestBody Product product) throws Exception {
        Product saved = adminService.addToElectricCategory(product);
        log.info("Product {} added to Electronics category", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /** Permanently delete a product. */
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) throws Exception {
        adminService.deleteProduct(id);
        log.info("Product {} deleted by admin", id);
        return ResponseEntity.noContent().build();
    }

    // ═════════════════════════════════════════════════════════════
    // DEALS MANAGEMENT
    // ═════════════════════════════════════════════════════════════

    @GetMapping("/deals")
    public ResponseEntity<List<Deal>> getAllDeals() throws Exception {
        return ResponseEntity.ok(adminService.getAllDeals());
    }

    @PostMapping("/deals")
    public ResponseEntity<Deal> createDeal(@RequestBody Deal deal) throws Exception {
        Deal created = adminService.createDeal(deal);
        log.info("Deal created: {}", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/deals/{id}")
    public ResponseEntity<Deal> updateDeal(
            @PathVariable Long id,
            @RequestBody Deal deal) throws Exception {
        Deal updated = adminService.updateDeal(id, deal);
        log.info("Deal {} updated", id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/deals/{id}")
    public ResponseEntity<Void> deleteDeal(@PathVariable Long id) throws Exception {
        adminService.deleteDeal(id);
        log.info("Deal {} deleted", id);
        return ResponseEntity.noContent().build();
    }

    // ═════════════════════════════════════════════════════════════
    // COUPON MANAGEMENT
    // ═════════════════════════════════════════════════════════════

    @GetMapping("/coupons")
    public ResponseEntity<List<Coupon>> getAllCoupons() throws Exception {
        return ResponseEntity.ok(adminService.getAllCoupons());
    }

    @PostMapping("/coupons")
    public ResponseEntity<Coupon> createCoupon(@RequestBody Coupon coupon) throws Exception {
        Coupon created = adminService.createCoupon(coupon);
        log.info("Coupon created: {}", created.getCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/coupons/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) throws Exception {
        adminService.deleteCoupon(id);
        log.info("Coupon {} deleted", id);
        return ResponseEntity.noContent().build();
    }
}