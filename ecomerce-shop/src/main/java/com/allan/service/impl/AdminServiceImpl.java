package com.allan.service.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.allan.config.JwtProvider;
import com.allan.domain.AccountStatus;
import com.allan.domain.CouponStatus;
import com.allan.domain.OrderStatus;
import com.allan.dto.OrderDTO;
import com.allan.model.Admin;
import com.allan.model.Coupon;
import com.allan.model.Deal;
import com.allan.model.Order;
import com.allan.model.Product;
import com.allan.model.Seller;
import com.allan.model.SellerReport;
import com.allan.model.VerificationCode;
import com.allan.repository.AdminRepository;
import com.allan.repository.CouponRepository;
import com.allan.repository.DealRepository;
import com.allan.repository.ProductRepository;
import com.allan.repository.SellerRepository;
import com.allan.repository.VerificationCodeRepository;
import com.allan.request.LoginRequest;
import com.allan.response.AuthResponse;
import com.allan.service.AdminService;
import com.allan.service.OrderService;
import com.allan.service.SellerReportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final SellerRepository sellerRepository;
    private final OrderService orderService;
    private final ProductRepository productRepository;
    private final DealRepository dealRepository;
    private final CouponRepository couponRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final SellerReportService sellerReportService;
    // ─────────────────────────────────────────────────────────────
    // AUTH
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Admin createAdmin(Admin admin) throws Exception {
        if (adminRepository.existsByEmail(admin.getEmail())) {
            throw new Exception("Admin with email " + admin.getEmail() + " already exists.");
        }
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        admin.setEmailVerified(false);
        admin.setCreatedAt(LocalDateTime.now());

        Admin savedAdmin = adminRepository.save(admin);
        log.info("Admin account created: {}", savedAdmin.getEmail());
        return savedAdmin;
    }

    @Override
    @Transactional
    public Admin verifyAdminEmail(String email, String otp) throws Exception {
        Admin admin = adminRepository.findByEmail(email);
        if (admin == null) {
            throw new Exception("No admin account found for email: " + email);
        }

        VerificationCode verificationCode = verificationCodeRepository.findByOtp(otp);
        if (verificationCode == null || !verificationCode.getEmail().equals(email)) {
            throw new Exception("Invalid or expired OTP.");
        }

        admin.setEmailVerified(true);
        admin.setUpdatedAt(LocalDateTime.now());
        verificationCodeRepository.delete(verificationCode);

        Admin verifiedAdmin = adminRepository.save(admin);
        log.info("Admin email verified: {}", email);
        return verifiedAdmin;
    }

    @Override
    public AuthResponse loginAdmin(LoginRequest loginRequest) throws Exception {
        Admin admin = adminRepository.findByEmail(loginRequest.getEmail());
        if (admin == null) {
            throw new Exception("Admin account not found.");
        }
        if (!admin.isEmailVerified()) {
            throw new Exception("Admin email is not verified. Please verify first.");
        }

        VerificationCode verificationCode = verificationCodeRepository
                .findByOtp(loginRequest.getOtp());
        if (verificationCode == null ||
                !verificationCode.getEmail().equals(loginRequest.getEmail())) {
            throw new Exception("Invalid OTP.");
        }
        verificationCodeRepository.delete(verificationCode);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                admin.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority(admin.getRole().toString()))
        );

        String token = jwtProvider.generateToken(authentication);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(token);
        authResponse.setMessage("Admin login successful.");
        authResponse.setRole(admin.getRole());

        log.info("Admin logged in: {}", admin.getEmail());
        return authResponse;
    }

    // ─────────────────────────────────────────────────────────────
    // SELLERS
    // ─────────────────────────────────────────────────────────────

    @Override
    public List<Seller> getAllSellers(AccountStatus status) throws Exception {
        if (status != null) {
            return sellerRepository.findByAccountStatus(status);
        }
        return sellerRepository.findAll();
    }

    @Override
    @Transactional
    public Seller updateSellerStatus(Long id, AccountStatus status) throws Exception {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new Exception("Seller not found with id: " + id));
        seller.setAccountStatus(status);
        Seller updated = sellerRepository.save(seller);
        log.info("Seller {} status updated to {}", id, status);
        return updated;
    }

    @Override
    @Transactional
    public void deleteSeller(Long id) throws Exception {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new Exception("Seller not found with id: " + id));
        sellerRepository.delete(seller);
        log.info("Seller {} deleted by admin", id);
    }

    @Override
    public SellerReport getSellerReport(Long sellerId) throws Exception {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new Exception("Seller not found with id: " + sellerId));

        // use orderService.sellersOrder() — matches existing OrderService interface
        List<OrderDTO> sellerOrders = orderService.sellersOrder(sellerId);

        Long totalOrders     = (long)sellerOrders.size();

        Long completedOrders = sellerOrders.stream()
                .filter(o -> OrderStatus.DELIVERED.equals(o.getOrderStatus()))
                .count();
        Long pendingOrders   = sellerOrders.stream()
                .filter(o -> OrderStatus.PENDING.equals(o.getOrderStatus()))
                .count();
        Long canceledOrders = sellerOrders.stream()
                .filter(o -> OrderStatus.CANCELLED.equals(o.getOrderStatus()))
                .count();

        // getTotalSellingPrice() matches actual field on Order model
        double totalRevenue = sellerOrders.stream()
                .filter(o -> OrderStatus.DELIVERED.equals(o.getOrderStatus()))
                .mapToDouble(o -> o.getTotalSellingPrice() != null ? o.getTotalSellingPrice() : 0.0)
                .sum();

        // count products by seller in memory — avoids needing countBySellerId in repo
        Long totalProducts = productRepository.findAll().stream()
                .filter(p -> p.getSeller() != null
                        && sellerId.equals(p.getSeller().getId()))
                .count();

        SellerReport report = sellerReportService.getSellerReport(seller);
        report.setTotalOrders(totalOrders);
        report.setCompletedOrders(completedOrders);
        report.setPendingOrders(pendingOrders);
        report.setCanceledOrders(canceledOrders);
        report.setTotalRevenue(totalRevenue);
        report.setTotalProducts(totalProducts);
        report.setGeneratedAt(LocalDateTime.now());

        return sellerReportService.updateSellerReport(report);
    }

    // ─────────────────────────────────────────────────────────────
    // ORDERS
    // ─────────────────────────────────────────────────────────────

   
    @Override
    public List<OrderDTO> getAllOrders(String status) throws Exception {
        return orderService.findAllOrders(status); // ✅ delegates, no Order entity ever touches AdminServiceImpl
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Long id, String status) throws Exception {
        OrderStatus orderStatus = validateOrderStatus(status);
        return orderService.updateOrderStatus(id, orderStatus);
    }

    private OrderStatus validateOrderStatus(String status) throws Exception {
        try {
            return OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid order status: " + status
                    + ". Valid values: " + Arrays.toString(OrderStatus.values()));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // PRODUCTS
    // ─────────────────────────────────────────────────────────────

    @Override
    public List<Product> getAllProducts(String category) throws Exception {
        if (category != null && !category.isBlank()) {
            // filter in memory — Product.category is @ManyToOne Category, not a String
            return productRepository.findAll().stream()
                    .filter(p -> p.getCategory() != null
                            && category.equalsIgnoreCase(p.getCategory().getName()))
                    .collect(Collectors.toList());
        }
        return productRepository.findAll();
    }

    @Override
    @Transactional
    public Product addToElectricCategory(Product product) throws Exception {
        product.setCreatedAt(LocalDateTime.now());
        Product saved = productRepository.save(product);
        log.info("Product {} added to Electronics category", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) throws Exception {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new Exception("Product not found with id: " + id));
        productRepository.delete(product);
        log.info("Product {} deleted by admin", id);
    }

    // ─────────────────────────────────────────────────────────────
    // DEALS
    // ─────────────────────────────────────────────────────────────

    @Override
    public List<Deal> getAllDeals() throws Exception {
        return dealRepository.findAll();
    }

    @Override
    @Transactional
    public Deal createDeal(Deal deal) throws Exception {
        if (deal.getDiscount() < 0 || deal.getDiscount() > 100) {
            throw new Exception("Deal discount must be between 0 and 100.");
        }
        Deal saved = dealRepository.save(deal);
        log.info("Deal created: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Deal updateDeal(Long id, Deal deal) throws Exception {
        Deal existing = dealRepository.findById(id)
                .orElseThrow(() -> new Exception("Deal not found with id: " + id));
        if (deal.getDiscount() < 0 || deal.getDiscount() > 100) {
            throw new Exception("Deal discount must be between 0 and 100.");
        }
        // only update fields that exist on your Deal model
        existing.setDiscount(deal.getDiscount());
        existing.setCategory(deal.getCategory());
        Deal updated = dealRepository.save(existing);
        log.info("Deal {} updated", id);
        return updated;
    }

    @Override
    @Transactional
    public void deleteDeal(Long id) throws Exception {
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() -> new Exception("Deal not found with id: " + id));
        dealRepository.delete(deal);
        log.info("Deal {} deleted", id);
    }

    // ─────────────────────────────────────────────────────────────
    // COUPONS
    // ─────────────────────────────────────────────────────────────

    @Override
    public List<Coupon> getAllCoupons() throws Exception {
        return couponRepository.findAll();
    }

    @Override
    @Transactional
    public Coupon createCoupon(Coupon coupon) throws Exception {

    // ── 1. Code uniqueness ────────────────────────────────────────────────
    String normalizedCode = coupon.getCode().toUpperCase().trim();
    if (couponRepository.findByCode(normalizedCode) != null) {
        throw new Exception("Coupon with code '" + normalizedCode + "' already exists.");
    }
    coupon.setCode(normalizedCode);

    // ── 2. Validate parent promotion exists ───────────────────────────────
    // Coupon is meaningless without a Promotion — the promotion holds
    // the discount value (on PromotionReward), not the coupon itself.
    if (coupon.getPromotion() == null || coupon.getPromotion().getId() == null) {
        throw new Exception("Coupon must be linked to a valid Promotion.");
    }

    // ── 3. Usage limit guard ──────────────────────────────────────────────
    // Vendors must always set an explicit limit — null (unlimited) is
    // only acceptable for admin platform coupons.
    if (coupon.getUsageLimit() != null && coupon.getUsageLimit() < 1) {
        throw new Exception("Usage limit must be at least 1 if specified.");
    }

    // ── 4. Per-customer limit guard ───────────────────────────────────────
    if (coupon.getUsagePerCustomer() != null && coupon.getUsagePerCustomer() < 1) {
        throw new Exception("Usage per customer must be at least 1 if specified.");
    }

    // ── 5. Safe defaults ──────────────────────────────────────────────────
    // Status and usedCount have defaults on the entity, but set them
    // explicitly here so the intent is visible at the service layer.
    coupon.setStatus(CouponStatus.ACTIVE);
    coupon.setUsedCount(0);

    Coupon saved = couponRepository.save(coupon);
    log.info("Coupon created: code={}, promotionId={}, usageLimit={}",
            saved.getCode(),
            saved.getPromotion().getId(),
            saved.getUsageLimit());
    return saved;
}

    @Override
    @Transactional
    public void deleteCoupon(Long id) throws Exception {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new Exception("Coupon not found with id: " + id));
        couponRepository.delete(coupon);
        log.info("Coupon {} deleted", id);
    }
}