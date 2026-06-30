package com.allan.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.allan.domain.AccountStatus;
import com.allan.dto.SellerProfileDTO;
import com.allan.dto.SellerPublicDTO;
import com.allan.dto.SellerReportDTO;
import com.allan.dto.SellerSummaryDTO;
import com.allan.exceptions.SellerException;
import com.allan.mapper.SellerMapper;
import com.allan.model.Seller;
import com.allan.model.SellerReport;
import com.allan.model.VerificationCode;
import com.allan.repository.VerificationCodeRepository;
import com.allan.request.LoginRequest;
import com.allan.response.AuthResponse;
import com.allan.service.AuthService;
import com.allan.service.EmailService;
import com.allan.service.SellerReportService;
import com.allan.service.SellerService;
import com.allan.utils.OtpUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sellers")
public class SellerController {

    private final EmailService emailService;
    private final AuthService authService;
    private final VerificationCodeRepository verificationCodeRepository;
    private final SellerService sellerService;
    private final SellerReportService sellerReportService;
    private final SellerMapper sellerMapper; // ✅ added

    // ─────────────────────────────────────────────
    // AUTH — unchanged, AuthResponse already a DTO
    // ─────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginSeller(@RequestBody VerificationCode req) throws Exception {
        String email = req.getEmail();
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("seller_" + email);
        loginRequest.setOtp(req.getOtp());

        AuthResponse authResponse = authService.signing(loginRequest);
        return ResponseEntity.ok(authResponse);
    }

    @PatchMapping("/verify/{otp}")
    public ResponseEntity<SellerProfileDTO> verifySellerEmail(@PathVariable String otp) throws Exception {
        if (otp == null || otp.isBlank()) {
            log.warn("Cannot verify seller with blank OTP");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        VerificationCode verificationCode = verificationCodeRepository.findByOtp(otp);

        if (verificationCode == null) {
            log.warn("Seller email verification failed — OTP not found in DB: {}", otp);
            throw new Exception("Invalid or expired OTP. Please request a new verification email.");
        }

        log.info("Verifying seller email: {} with OTP: {}", verificationCode.getEmail(), otp);
        Seller seller = sellerService.verifyEmail(verificationCode.getEmail(), otp);

        // ✅ this is the seller's own data right after verification — profile DTO is appropriate
        return new ResponseEntity<>(sellerMapper.toProfileDTO(seller), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<SellerProfileDTO> createSeller(@RequestBody Seller seller) throws Exception {
        Seller savedSeller = sellerService.createSeller(seller);

        String otp = OtpUtil.generateOtp();
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setOtp(otp);
        verificationCode.setEmail(seller.getEmail());
        verificationCodeRepository.save(verificationCode);

        String subject = "Huru Bazar Email Verification Code";
        String text = "Welcome to Huru Bazar, Verify your account using this Link";
        String frontend_url = "http://localhost:3000/verify-seller";
        emailService.sendVerificationOtpEmail(seller.getEmail(), verificationCode.getOtp(), subject,
                text + frontend_url);

        return new ResponseEntity<>(sellerMapper.toProfileDTO(savedSeller), HttpStatus.CREATED);
    }

    // ─────────────────────────────────────────────
    // PUBLIC VIEW — anyone can see (storefront)
    // ─────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<SellerPublicDTO> getSellerById(@PathVariable Long id) throws SellerException {
        Seller seller = sellerService.getSellerbyId(id);
        return new ResponseEntity<>(sellerMapper.toPublicDTO(seller), HttpStatus.OK);
    }

    // ─────────────────────────────────────────────
    // PRIVATE VIEW — seller's own profile only
    // ─────────────────────────────────────────────

    @GetMapping("/profile")
    public ResponseEntity<?> getSellerByJwt(@RequestHeader("Authorization") String jwt) throws Exception {
        try {
            Seller seller = sellerService.getSellerProfile(jwt);
            return new ResponseEntity<>(sellerMapper.toProfileDTO(seller), HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/report")
    public ResponseEntity<SellerReportDTO> getSellerReport(
            @RequestHeader("Authorization") String jwt) throws Exception {
        Seller seller = sellerService.getSellerProfile(jwt);
        SellerReport report = sellerReportService.getSellerReport(seller);
        return new ResponseEntity<>(sellerMapper.toReportDTO(report), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SellerProfileDTO> updateSellerById(
            @PathVariable Long id,
            @RequestBody Seller seller) throws Exception {
        Seller updatedSeller = sellerService.updateSeller(id, seller);
        return ResponseEntity.ok(sellerMapper.toProfileDTO(updatedSeller));
    }

    @PatchMapping()
    public ResponseEntity<SellerProfileDTO> updateSeller(
            @RequestHeader("Authorization") String jwt,
            @RequestBody Seller seller) throws Exception {
        Seller profile = sellerService.getSellerProfile(jwt);
        Seller updatedSeller = sellerService.updateSeller(profile.getId(), seller);
        return ResponseEntity.ok(sellerMapper.toProfileDTO(updatedSeller));
    }

    // ─────────────────────────────────────────────
    // ADMIN LISTING VIEW — lighter summary, no bank details
    // ─────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<SellerSummaryDTO>> getAllSellers(
            @RequestParam(required = false) AccountStatus status) {
        List<Seller> sellers = sellerService.getAllSellers(status);
        return ResponseEntity.ok(sellerMapper.toSummaryDTOList(sellers));
    }

    /**
 * Delete a seller. If the seller has dependent products/orders,
 * the account is soft-deleted (status set to DEACTIVATED) instead
 * of being physically removed, to preserve referential integrity.
 */
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteSeller(@PathVariable Long id) throws Exception {
    sellerService.deleteSeller(id);
    log.info("Delete request processed for seller {}", id);
    return ResponseEntity.noContent().build();
}



    // @DeleteMapping("/{id}")
    // public ResponseEntity<Void> deleteSeller(@PathVariable Long id) throws Exception {
    //     sellerService.deleteSeller(id);
    //     return ResponseEntity.noContent().build();
    // }
}







// package com.allan.controller;

// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PatchMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestHeader;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;

// import com.allan.domain.AccountStatus;
// import com.allan.exceptions.SellerException;
// import com.allan.model.Seller;
// import com.allan.model.SellerReport;
// import com.allan.model.VerificationCode;
// import com.allan.repository.VerificationCodeRepository;
// import com.allan.request.LoginRequest;
// import com.allan.response.AuthResponse;
// import com.allan.service.AuthService;
// import com.allan.service.EmailService;
// import com.allan.service.SellerReportService;
// import com.allan.service.SellerService;
// import com.allan.utils.OtpUtil;

// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;

// @Slf4j
// @RestController
// @RequiredArgsConstructor
// @RequestMapping("/sellers")
// public class SellerController {

//     private final EmailService emailService;
//     private final AuthService authService;
//     private final VerificationCodeRepository verificationCodeRepository;
//     private final SellerService sellerService;
//     private final SellerReportService sellerReportService;

//     @PostMapping("/login")
//     public ResponseEntity<AuthResponse> loginSeller(@RequestBody VerificationCode req) throws Exception {

//         // String otp = req.getOtp();
//         String email = req.getEmail();
//         LoginRequest loginRequest = new LoginRequest();
//         loginRequest.setEmail("seller_" + email);
//         loginRequest.setOtp(req.getOtp());

//         // req.setEmail("seller_" + email);
//         AuthResponse authResponse = authService.signing(loginRequest);
//         return ResponseEntity.ok(authResponse);
//     }

//     @PatchMapping("/verify/{otp}")
//     public ResponseEntity<Seller> verifySellerEmail(@PathVariable String otp) throws Exception {

//         // Guard blank OTP

//         if(otp == null || otp.isBlank()) {
//             log.warn ("Can notverify Seller with blank Otp");

//             return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//         }

//         VerificationCode verificationCode = verificationCodeRepository.findByOtp(otp);

//        // ✅ detailed logging to diagnose OTP failures
//     if (verificationCode == null) {
//         log.warn("Seller email verification failed — OTP not found in DB: {}", otp);
//         throw new Exception("Invalid or expired OTP. Please request a new verification email.");
//     }

//     // ✅ redundant check removed — if findByOtp returned it, the OTP already matches
//     // the original code checked verificationCode.getOtp().equals(otp) which is always
//     // true if findByOtp found it — this was masking the real issue

//     log.info("Verifying seller email: {} with OTP: {}", verificationCode.getEmail(), otp);

//         Seller seller = sellerService.verifyEmail(verificationCode.getEmail(), otp);

//         return new ResponseEntity<>(seller, HttpStatus.OK);
//     }

//     @PostMapping
//     public ResponseEntity<Seller> createSeller(@RequestBody Seller seller) throws Exception {
//         Seller savedSeller = sellerService.createSeller(seller);

//         String otp = OtpUtil.generateOtp();

//         VerificationCode verificationCode = new VerificationCode();
//         verificationCode.setOtp(otp);
//         verificationCode.setEmail(seller.getEmail());
//         verificationCodeRepository.save(verificationCode);

//         String subject = "Huru Bazar Email Verification Cade";
//         String text = "Welcome to Huru Bazar, Verify your account using this Link";
//         String frontend_url = "http://localhost:3000/verify-seller";
//         emailService.sendVerificationOtpEmail(seller.getEmail(), verificationCode.getOtp(), subject,
//                 text + frontend_url);

//         return new ResponseEntity<>(savedSeller, HttpStatus.CREATED);
//     }

//     @GetMapping("/{id}")
//     public ResponseEntity<Seller> getSellerById(@PathVariable Long id) throws SellerException {
//         Seller seller = sellerService.getSellerbyId(id);
//         return new ResponseEntity<>(seller, HttpStatus.OK);
//     }

//     @GetMapping("/profile")
//     public ResponseEntity<?> getSellerByJwt(@RequestHeader("Authorization") String jwt) throws Exception {

//         try {
//             Seller seller = sellerService.getSellerProfile(jwt);
//             return new ResponseEntity<>(seller, HttpStatus.OK);
//         } catch (Exception e) {
//             Map<String, String> error = new HashMap<>();
//             error.put("error", e.getMessage());
//             return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
//         }
//     }

//     @GetMapping("/report")
//     public ResponseEntity<SellerReport> getSellerReport(@RequestHeader("Authorization") String jwt) throws Exception {
//         Seller seller = sellerService.getSellerProfile(jwt);
//         SellerReport report = sellerReportService.getSellerReport(seller);
//         return new ResponseEntity<>(report, HttpStatus.OK);
//     }

//     @PutMapping("/{id}")
//     public ResponseEntity<Seller> updateSellerById(
//             @PathVariable Long id,
//             @RequestBody Seller seller) throws Exception {
//         Seller updatedSeller = sellerService.updateSeller(id, seller);
//         return ResponseEntity.ok(updatedSeller);
//     }

//     @GetMapping
//     public ResponseEntity<List<Seller>> getAllSellers(@RequestParam(required = false) AccountStatus status) {
//         List<Seller> sellers = sellerService.getAllSellers(status);
//         return ResponseEntity.ok(sellers);
//     }

//     @PatchMapping()
//     public ResponseEntity<Seller> updateSeller(
//             @RequestHeader("Authorization") String jwt,
//             @RequestBody Seller seller) throws Exception {
//         Seller profile = sellerService.getSellerProfile(jwt);
//         Seller updatedSeller = sellerService.updateSeller(profile.getId(), seller);
//         return ResponseEntity.ok(updatedSeller);
//     }

//     @DeleteMapping("/{id}")
//     public ResponseEntity<Void> deleteSeller(@PathVariable Long id) throws Exception {
//         sellerService.deleteSeller(id);
//         return ResponseEntity.noContent().build();
//     }

// }
