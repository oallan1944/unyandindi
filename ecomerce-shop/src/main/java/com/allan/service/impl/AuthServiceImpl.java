package com.allan.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.allan.config.JwtProvider;
import com.allan.domain.USER_ROLE;
import com.allan.model.Cart;
import com.allan.model.Seller;
import com.allan.model.User;
import com.allan.model.VerificationCode;
import com.allan.repository.AdminRepository;
import com.allan.repository.CartRepository;
import com.allan.repository.SellerRepository;
import com.allan.repository.UserRepository;
import com.allan.repository.VerificationCodeRepository;
import com.allan.request.LoginRequest;
import com.allan.response.AuthResponse;
import com.allan.request.SignupRequest;
import com.allan.service.AuthService;
import com.allan.service.EmailService;
import com.allan.utils.OtpUtil;
import com.allan.model.Admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartRepository cartRepository;
    private final JwtProvider jwtProvider;
    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;
    private final CustomUserServiceImpl customUserService;
    private final SellerRepository sellerRepository;
    

    // ─────────────────────────────────────────────
    // SIGNUP
    // ─────────────────────────────────────────────

    @Override
    @Transactional
    public String createUser(SignupRequest req) throws Exception {

        // ✅ Optional.orElse(null) — clean unwrap for null check
        VerificationCode verificationCode = verificationCodeRepository
                .findByEmail(req.getEmail())
                .orElse(null);

        if (verificationCode == null || !verificationCode.getOtp().equals(req.getOtp())) {
            log.warn("Signup OTP mismatch for email: {}", req.getEmail());
            throw new Exception("Wrong OTP.");
        }

        User user = userRepository.findByEmail(req.getEmail());
        if (user == null) {
            User createdUser = new User();
            createdUser.setEmail(req.getEmail());
            createdUser.setFullName(req.getFullName());
            createdUser.setRole(USER_ROLE.ROLE_CUSTOMER);
            createdUser.setMobile("");
            createdUser.setPassword(passwordEncoder.encode(req.getOtp()));

            user = userRepository.save(createdUser);

            Cart cart = new Cart();
            cart.setUser(user);
            cartRepository.save(cart);

            log.info("New user created: {}", user.getEmail());
        }

        // ✅ consume OTP after successful signup
        verificationCodeRepository.delete(verificationCode);

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(USER_ROLE.ROLE_CUSTOMER.toString()));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                req.getEmail(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return jwtProvider.generateToken(authentication);
    }

    // ─────────────────────────────────────────────
    // SEND OTP
    // ─────────────────────────────────────────────

    @Override
    @Transactional
    public void sentLoginOtp(String email, USER_ROLE role) throws Exception {

        final String SIGNING_PREFIX = "signing_";

        if (email.startsWith(SIGNING_PREFIX)) {
            email = email.substring(SIGNING_PREFIX.length());

            if (role.equals(USER_ROLE.ROLE_SELLER)) {
                Seller seller = sellerRepository.findByEmail(email);
                if (seller == null) {
                    throw new Exception("Seller not found for email: " + email);
                }
            } else {
                User user = userRepository.findByEmail(email);
                if (user == null) {
                    throw new Exception("User not found for email: " + email);
                }
            }
        }

        // ✅ Optional.ifPresent — clean delete of existing OTP without null check
        verificationCodeRepository.findByEmail(email)
                .ifPresent(verificationCodeRepository::delete);

        String otp = OtpUtil.generateOtp();
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setOtp(otp);
        verificationCode.setEmail(email);
        verificationCodeRepository.save(verificationCode);

        String subject = "Huru Bazar Login/Signup OTP";
        String text = "Your Login/Signup OTP is: " + otp +
                "\nPlease do not share this OTP with anyone.";
        emailService.sendVerificationOtpEmail(email, otp, subject, text);

        log.info("OTP sent to: {}", email);
    }

    // ─────────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────────

    @Override
    public AuthResponse signing(LoginRequest req) throws Exception {
        String username = req.getEmail();
        String otp = req.getOtp();

        Authentication authentication = authenticate(username, otp);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtProvider.generateToken(authentication);

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String roleName = authorities.isEmpty()
                ? null
                : authorities.iterator().next().getAuthority();

        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(token);
        authResponse.setMessage("Login successful.");
        authResponse.setRole(USER_ROLE.valueOf(roleName));

        log.info("User logged in: {}", username);
        return authResponse;
    }

    // ─────────────────────────────────────────────
    // AUTHENTICATE
    // ─────────────────────────────────────────────

    private Authentication authenticate(String username, String otp) throws Exception {
        UserDetails userDetails = customUserService.loadUserByUsername(username);

        if (userDetails == null) {
            throw new BadCredentialsException("Invalid username.");
        }

        final String SELLER_PREFIX = "seller_";
        String emailForOtpLookup = username; 

        // ✅ strip seller_ prefix for OTP lookup — OTP is saved against plain email
        if (username.startsWith(SELLER_PREFIX)) {
            emailForOtpLookup = username.substring(SELLER_PREFIX.length());
            

            Seller seller = sellerRepository.findByEmail(emailForOtpLookup);
            if (seller == null) {
                throw new Exception("Seller not found for email: " + emailForOtpLookup);
            }
        }

        

        // ✅ Optional.orElse(null) — clean unwrap for null check
        VerificationCode verificationCode = verificationCodeRepository
                .findByEmail(emailForOtpLookup)
                .orElse(null);

            log.info("Username = {}", username);
            log.info("Email used for OTP lookup = {}", emailForOtpLookup);

        if (verificationCode == null || !verificationCode.getOtp().equals(otp)) {
            log.warn("OTP mismatch for: {}", emailForOtpLookup);
            throw new Exception("OTP does not match.");
        }

        // ✅ consume OTP after successful login — prevents replay attacks
        verificationCodeRepository.delete(verificationCode);

        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }
}



