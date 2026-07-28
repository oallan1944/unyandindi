package com.allan.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.allan.domain.AuthProvider;
import com.allan.domain.USER_ROLE;
import com.allan.model.Admin;
import com.allan.model.Seller;
import com.allan.model.User;
import com.allan.repository.AdminRepository;
import com.allan.repository.SellerRepository;
import com.allan.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomUserServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final AdminRepository adminRepository;

    private static final String SELLER_PREFIX = "seller_";

    // ✅ sentinel password used for Google-only accounts — a string that
    // can never be produced by BCrypt and can never match any real OTP.
    // AuthServiceImpl.authenticate checks for this sentinel and rejects
    // OTP login with a clear message before even attempting OTP lookup.
    private static final String GOOGLE_ONLY_SENTINEL =
            "{noop}GOOGLE_ONLY_NO_OTP";

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        log.info("loadUserByUsername called with: {}", username);

        // ── Seller ───────────────────────────────────────────────
        if (username.startsWith(SELLER_PREFIX)) {
            String actualEmail =
                    username.substring(SELLER_PREFIX.length());
            Seller seller = sellerRepository.findByEmail(actualEmail);
            if (seller != null) {
                return buildUserDetails(seller.getEmail(),
                        seller.getPassword(), seller.getRole());
            }
        }

        // ── Admin ────────────────────────────────────────────────
        Admin admin = adminRepository.findByEmail(username);
        if (admin != null) {
            return buildUserDetails(admin.getEmail(),
                    admin.getPassword(), admin.getRole());
        }

        // ── Customer ─────────────────────────────────────────────
        User user = userRepository.findByEmail(username);
        if (user != null) {

            // ✅ Google-primary user with OTP disabled — replace the
            // empty password with the sentinel string so AuthServiceImpl
            // can detect and reject OTP login attempts with a clear message.
            // Without this, Spring Security would compare the submitted OTP
            // against an empty BCrypt hash and silently fail with a
            // misleading "bad credentials" error instead of explaining
            // that this account uses Google sign-in.
            if (AuthProvider.GOOGLE.equals(user.getPrimaryAuthProvider())
                    && !user.isOtpEnabled()
                    && (user.getPassword() == null
                            || user.getPassword().isBlank())) {
                return buildUserDetails(user.getEmail(),
                        GOOGLE_ONLY_SENTINEL, user.getRole());
            }

            // ✅ Google user WITH otpEnabled=true (linked account or
            // Google primary who has verified OTP fallback) — password
            // is still empty string but OTP flow is valid for them.
            // Return normally — AuthServiceImpl will validate the OTP
            // from VerificationCode table, not from this password field.
            return buildUserDetails(user.getEmail(),
                    user.getPassword() != null ? user.getPassword() : "",
                    user.getRole());
        }

        throw new UsernameNotFoundException(
                "User not found with username: " + username);
    }

    private UserDetails buildUserDetails(String email,
                                          String password,
                                          USER_ROLE role) {
        if (role == null) {
            role = USER_ROLE.ROLE_CUSTOMER;
        }
        List<GrantedAuthority> authorityList = new ArrayList<>();
        authorityList.add(new SimpleGrantedAuthority(role.toString()));
        return new org.springframework.security.core.userdetails.User(
                email, password, authorityList);
    }
}