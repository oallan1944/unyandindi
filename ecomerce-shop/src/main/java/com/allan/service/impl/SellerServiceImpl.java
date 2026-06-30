package com.allan.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.allan.config.JwtProvider;
import com.allan.domain.AccountStatus;
import com.allan.domain.USER_ROLE;
import com.allan.exceptions.SellerException;
import com.allan.model.Seller;
import com.allan.repository.SellerRepository;
import com.allan.repository.VerificationCodeRepository;
import com.allan.service.SellerService;
import com.allan.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {

    private final SellerRepository sellerRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final ProductRepository productRepository;

    // ─────────────────────────────────────────────
    // AUTH
    // ─────────────────────────────────────────────

    @Override
    public Seller getSellerProfile(String jwt) throws Exception {
        String email = jwtProvider.getEmailFromJwtToken(jwt);
        log.debug("Fetching seller profile for email: {}", email);
        return getSellerByEmail(email);
    }

    @Override
    @Transactional
    public Seller createSeller(Seller seller) throws Exception {
        if (sellerRepository.findByEmail(seller.getEmail()) != null) {
            throw new Exception("Seller already exists, use another email.");
        }

        Seller newSeller = new Seller();
        newSeller.setEmail(seller.getEmail());
        newSeller.setPassword(passwordEncoder.encode(seller.getPassword()));
        newSeller.setSellerName(seller.getSellerName());
        newSeller.setPickupAddress(seller.getPickupAddress());
        newSeller.setGSTIN(seller.getGSTIN());
        newSeller.setRole(USER_ROLE.ROLE_SELLER);
        newSeller.setMobile(seller.getMobile());
        newSeller.setBankDetails(seller.getBankDetails());
        newSeller.setBusinessDetails(seller.getBusinessDetails());

        // ✅ fixed: was setting on original 'seller' object, not 'newSeller'
        newSeller.setEmailVerified(false);
        newSeller.setAccountStatus(AccountStatus.PENDING_VERIFICATION);
        newSeller.setCreatedAt(LocalDateTime.now());

        Seller saved = sellerRepository.save(newSeller);
        log.info("Seller created: id={} email={}", saved.getId(), saved.getEmail());
        return saved;
    }

    @Override
    @Transactional
    public Seller verifyEmail(String email, String otp) throws Exception {
        Seller seller = getSellerByEmail(email);

        if (seller.isEmailVerified()) {
            log.warn("Seller email already verified: {}", email);
            return seller; // ✅ idempotent — don't throw if already verified
        }

        seller.setEmailVerified(true);
        seller.setAccountStatus(AccountStatus.ACTIVE); // ✅ activate seller on verification
        seller.setUpdatedAt(LocalDateTime.now());

        // ✅ consume OTP after successful verification — prevents replay attacks
        verificationCodeRepository.findByEmail(email);
               // .ifPresent(verificationCodeRepository::delete);

        Seller verified = sellerRepository.save(seller);
        log.info("Seller email verified: {}", email);
        return verified;
    }

    // ─────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────

    @Override
    public Seller getSellerbyId(Long id) throws SellerException {
        return sellerRepository.findById(id)
                .orElseThrow(() -> new SellerException("Seller not found with id: " + id));
    }

    @Override
    public Seller getSellerByEmail(String email) throws Exception {
        Seller seller = sellerRepository.findByEmail(email);
        if (seller == null) {
            throw new Exception("Seller not found for email: " + email);
        }
        return seller;
    }

    @Override
    public List<Seller> getAllSellers(AccountStatus status) {
        return status != null
                ? sellerRepository.findByAccountStatus(status)
                : sellerRepository.findAll();
    }

    // ─────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────

    @Override
    @Transactional
    public Seller updateSeller(Long id, Seller updates) throws Exception {
        Seller existing = getSellerbyId(id);

        if (updates.getSellerName() != null)
            existing.setSellerName(updates.getSellerName());
        if (updates.getMobile() != null)
            existing.setMobile(updates.getMobile());
        if (updates.getEmail() != null)
            existing.setEmail(updates.getEmail());
        if (updates.getGSTIN() != null)
            existing.setGSTIN(updates.getGSTIN());

        if (updates.getBusinessDetails() != null
                && updates.getBusinessDetails().getBusinessName() != null) {
            existing.getBusinessDetails()
                    .setBusinessName(updates.getBusinessDetails().getBusinessName());
        }

        if (updates.getBankDetails() != null) {
            var bank = updates.getBankDetails();
            if (bank.getAccountHolderName() != null)
                existing.getBankDetails().setAccountHolderName(bank.getAccountHolderName());
            if (bank.getAccountNumber() != null)
                existing.getBankDetails().setAccountNumber(bank.getAccountNumber());
            if (bank.getIfscCode() != null)
                existing.getBankDetails().setIfscCode(bank.getIfscCode());
        }

        if (updates.getPickupAddress() != null) {
            var address = updates.getPickupAddress();
            if (address.getAddress() != null)
                existing.getPickupAddress().setAddress(address.getAddress());
            if (address.getCity() != null)
                existing.getPickupAddress().setCity(address.getCity());
            if (address.getState() != null)
                existing.getPickupAddress().setState(address.getState());
            if (address.getMobile() != null)
                existing.getPickupAddress().setMobile(address.getMobile());
            if (address.getPinCode() != null)
                existing.getPickupAddress().setPinCode(address.getPinCode());
        }

        existing.setUpdatedAt(LocalDateTime.now());
        Seller updated = sellerRepository.save(existing);
        log.info("Seller updated: id={}", id);
        return updated;
    }

    @Override
    @Transactional
    public Seller updateSellerAccountStatus(Long sellerId, AccountStatus status) throws Exception {
        Seller seller = getSellerbyId(sellerId);
        seller.setAccountStatus(status);
        seller.setUpdatedAt(LocalDateTime.now());
        Seller updated = sellerRepository.save(seller);
        log.info("Seller {} status updated to {}", sellerId, status);
        return updated;
    }

    // ─────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────

    @Override
@Transactional
public void deleteSeller(Long id) throws Exception {
    Seller seller = getSellerbyId(id);

    // ✅ check for dependent data before attempting hard delete
    long productCount = productRepository.countBySellerId(id);

    if (productCount > 0) {
        // ✅ soft delete — deactivate instead of removing, preserves referential integrity
        seller.setAccountStatus(AccountStatus.DEACTIVATED);
        seller.setUpdatedAt(LocalDateTime.now());
        sellerRepository.save(seller);
        log.info("Seller {} soft-deleted (deactivated) — has {} dependent products",
                id, productCount);
        return;
    }

    // ✅ safe to hard delete — no dependent products
    sellerRepository.delete(seller);
    log.info("Seller {} permanently deleted — no dependent data found", id);
}

}





// package com.allan.service.impl;

// import java.util.List;

// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.stereotype.Service;

// import com.allan.config.JwtProvider;
// import com.allan.domain.AccountStatus;
// import com.allan.domain.USER_ROLE;
// import com.allan.exceptions.SellerException;
// import com.allan.model.Seller;
// import com.allan.repository.SellerRepository;
// import com.allan.service.SellerService;

// import lombok.RequiredArgsConstructor;

// @Service
// @RequiredArgsConstructor
// public class SellerServiceImpl implements SellerService {

//     private final SellerRepository sellerRepository;
//     private final JwtProvider jwtProvider;
//     private final PasswordEncoder passwordEncoder;

//     @Override
//     public Seller getSellerProfile(String jwt) throws Exception {
//         // if (jwt.startsWith("Bearer ")) {
//         // jwt = jwt.substring(7);
//         // }
//         String email = jwtProvider.getEmailFromJwtToken(jwt);
//         System.out.println("Email extracted from JWT: " + email);
//         return getSellerByEmail(email);
//     }

//     @Override
//     public Seller createSeller(Seller seller) throws Exception {
//         if (sellerRepository.findByEmail(seller.getEmail()) != null) {
//             throw new Exception("Seller already exists, use another email");
//         }

//         // Encode password and set defaults
//         Seller newSeller = new Seller();
//         newSeller.setEmail(seller.getEmail());
//         newSeller.setPassword(passwordEncoder.encode(seller.getPassword()));
//         newSeller.setSellerName(seller.getSellerName());
//         newSeller.setPickupAddress(seller.getPickupAddress());
//         newSeller.setGSTIN(seller.getGSTIN());
//         newSeller.setRole(USER_ROLE.ROLE_SELLER);
//         newSeller.setMobile(seller.getMobile());
//         newSeller.setBankDetails(seller.getBankDetails());
//         newSeller.setBusinessDetails(seller.getBusinessDetails());

//         // added this two lines
//         seller.setEmailVerified(false);
//         seller.setAccountStatus(AccountStatus.PENDING_VERIFICATION);

//         // Save seller (pickupAddress will cascade automatically)
//         return sellerRepository.save(newSeller);
//     }

//     @Override
//     public Seller getSellerbyId(Long id) throws SellerException {
//         return sellerRepository.findById(id)
//                 .orElseThrow(() -> new SellerException("Seller not found with id: " + id));
//     }

//     @Override
//     public Seller getSellerByEmail(String email) throws Exception {
//         Seller seller = sellerRepository.findByEmail(email);
//         if (seller == null) {
//             throw new Exception("Seller not found for email: " + email);
//         }
//         return seller;
//     }

//     @Override
//     public List<Seller> getAllSellers(AccountStatus status) {
//         return status != null
//                 ? sellerRepository.findByAccountStatus(status)
//                 : sellerRepository.findAll();
//     }

//     @Override
//     public Seller updateSeller(Long id, Seller updates) throws Exception {
//         Seller existing = getSellerbyId(id);

//         // Update basic info
//         if (updates.getSellerName() != null)
//             existing.setSellerName(updates.getSellerName());
//         if (updates.getMobile() != null)
//             existing.setMobile(updates.getMobile());
//         if (updates.getEmail() != null)
//             existing.setEmail(updates.getEmail());
//         if (updates.getGSTIN() != null)
//             existing.setGSTIN(updates.getGSTIN());

//         // Update business details
//         if (updates.getBusinessDetails() != null && updates.getBusinessDetails().getBusinessName() != null) {
//             existing.getBusinessDetails().setBusinessName(updates.getBusinessDetails().getBusinessName());
//         }

//         // Update bank details
//         if (updates.getBankDetails() != null) {
//             var bank = updates.getBankDetails();
//             if (bank.getAccountHolderName() != null)
//                 existing.getBankDetails().setAccountHolderName(bank.getAccountHolderName());
//             if (bank.getAccountNumber() != null)
//                 existing.getBankDetails().setAccountNumber(bank.getAccountNumber());
//             if (bank.getIfscCode() != null)
//                 existing.getBankDetails().setIfscCode(bank.getIfscCode());
//         }

//         // Update pickup address
//         if (updates.getPickupAddress() != null) {
//             var address = updates.getPickupAddress();
//             if (address.getAddress() != null)
//                 existing.getPickupAddress().setAddress(address.getAddress());
//             if (address.getCity() != null)
//                 existing.getPickupAddress().setCity(address.getCity());
//             if (address.getState() != null)
//                 existing.getPickupAddress().setState(address.getState());
//             if (address.getMobile() != null)
//                 existing.getPickupAddress().setMobile(address.getMobile());
//             if (address.getPinCode() != null)
//                 existing.getPickupAddress().setPinCode(address.getPinCode());
//         }

//         return sellerRepository.save(existing);
//     }

//     @Override
//     public void deleteSeller(Long id) throws Exception {
//         Seller seller = getSellerbyId(id);
//         sellerRepository.delete(seller);
//     }

//     @Override
//     public Seller verifyEmail(String email, String otp) throws Exception {
//         Seller seller = getSellerByEmail(email);
//         seller.setEmailVerified(true);
//         return sellerRepository.save(seller);
//     }

//     @Override
//     public Seller updateSellerAccountStatus(Long sellerId, AccountStatus status) throws Exception {
//         Seller seller = getSellerbyId(sellerId);
//         seller.setAccountStatus(status);
//         return sellerRepository.save(seller);
//     }
// }
