package com.allan.service.impl;

import java.time.LocalDateTime;
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
import com.allan.domain.AuthProvider;
import com.allan.domain.USER_ROLE;
import com.allan.exceptions.GoogleOnlyAccountException;
import com.allan.exceptions.InvalidOtpException;
import com.allan.exceptions.OtpExpiredException;
import com.allan.exceptions.ResourceNotFoundException;
import com.allan.exceptions.TooManyAttemptsException;
import com.allan.model.Admin;
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
import com.allan.request.SignupRequest;
import com.allan.response.AuthResponse;
import com.allan.service.AuthService;
import com.allan.service.EmailService;
import com.allan.utils.OtpUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int OTP_VALIDITY_MINUTES = 5;
    private static final int MAX_OTP_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartRepository cartRepository;
    private final JwtProvider jwtProvider;
    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;
    private final CustomUserServiceImpl customUserService;
    private final SellerRepository sellerRepository;
    private final AdminRepository adminRepository;

    // ─────────────────────────────────────────────
    // SIGNUP
    // ─────────────────────────────────────────────

    @Override
    @Transactional
    public String createUser(SignupRequest req) throws Exception {

        VerificationCode verificationCode = latestVerificationCode(req.getEmail());
        validateOtp(verificationCode, req.getOtp(), req.getEmail());

        User user = userRepository.findByEmail(req.getEmail());
        if (user == null) {
            User createdUser = new User();
            createdUser.setEmail(req.getEmail());
            createdUser.setFullName(req.getFullName());
            createdUser.setRole(USER_ROLE.ROLE_CUSTOMER);
            createdUser.setMobile("");
            createdUser.setPassword(passwordEncoder.encode(req.getOtp()));

            // ✅ set all new fields — prevents NOT NULL constraint violations
            // and ensures account-linking logic has correct defaults
            createdUser.setPrimaryAuthProvider(AuthProvider.LOCAL);
            createdUser.setGoogleEnabled(false);
            createdUser.setOtpEnabled(true);
            // ✅ LOCAL signup = profile is complete — user provided fullName
            // via signup form and will add mobile/address at first purchase
            createdUser.setProfileComplete(true);
            createdUser.setEmailVerified(true); // OTP verification = email verified
            createdUser.setCreatedAt(LocalDateTime.now());

            user = userRepository.save(createdUser);

            Cart cart = new Cart();
            cart.setUser(user);
            cartRepository.save(cart);

            log.info("New LOCAL user created: {}", mask(user.getEmail()));
        }

        verificationCodeRepository.deleteAllByEmail(req.getEmail());

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(
                USER_ROLE.ROLE_CUSTOMER.toString()));

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

            // Enum-first comparison: safe even when role is null, instead
            // of role.equals(...) which threw NullPointerException (and
            // therefore a raw 500) for any caller that omitted role.
            if (USER_ROLE.ROLE_SELLER.equals(role)) {
                Seller seller = sellerRepository.findByEmail(email);
                if (seller == null) {
                    throw new ResourceNotFoundException(
                            "No seller account found for this email.");
                }
            } else if (USER_ROLE.ROLE_ADMIN.equals(role)) {
                Admin admin = adminRepository.findByEmail(email);
                if (admin == null) {
                    throw new ResourceNotFoundException(
                            "No admin account found for this email.");
                }
            } else {
                User user = userRepository.findByEmail(email);
                if (user == null) {
                    throw new ResourceNotFoundException(
                            "No account found for this email.");
                }

                // ✅ Google-primary users with otpEnabled=true can use OTP
                // as a fallback. This is intentional — it's the safety net
                // that lets them sign in if they lose Google access.
                // No special handling needed here — OTP flow is the same.
            }
        }

        // Bulk delete — cannot throw NonUniqueResultException on multiple
        // matching rows, unlike the previous find-then-delete pattern.
        verificationCodeRepository.deleteAllByEmail(email);

        String otp = OtpUtil.generateOtp();
        LocalDateTime now = LocalDateTime.now();

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setOtp(otp);
        verificationCode.setEmail(email);
        verificationCode.setCreatedAt(now);
        verificationCode.setExpiresAt(now.plusMinutes(OTP_VALIDITY_MINUTES));
        verificationCode.setAttempts(0);
        verificationCodeRepository.save(verificationCode);

        String subject = "Huru Login/Signup OTP";
        String text = "Your Login/Signup OTP is: " + otp
                + "\nThis code expires in " + OTP_VALIDITY_MINUTES + " minutes."
                + "\nPlease do not share this OTP with anyone.";
        emailService.sendVerificationOtpEmail(email, otp, subject, text);

        log.info("OTP sent to: {}", mask(email));
    }

    // ─────────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse signing(LoginRequest req) throws Exception {
        String username = req.getEmail();
        String otp = req.getOtp();

        Authentication authentication = authenticate(username, otp);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtProvider.generateToken(authentication);

        Collection<? extends GrantedAuthority> authorities =
                authentication.getAuthorities();
        String roleName = authorities.isEmpty()
                ? null
                : authorities.iterator().next().getAuthority();

        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(token);
        authResponse.setMessage("Login successful.");
        authResponse.setRole(USER_ROLE.valueOf(roleName));

        log.info("User logged in: {}", mask(username));
        return authResponse;
    }

    // ─────────────────────────────────────────────
    // AUTHENTICATE
    // ─────────────────────────────────────────────

    private Authentication authenticate(String username,
                                         String otp) throws Exception {
        UserDetails userDetails =
                customUserService.loadUserByUsername(username);

        if (userDetails == null) {
            throw new BadCredentialsException("Invalid username.");
        }

        final String SELLER_PREFIX = "seller_";
        String emailForOtpLookup = username;

        if (username.startsWith(SELLER_PREFIX)) {
            emailForOtpLookup = username.substring(SELLER_PREFIX.length());

            Seller seller = sellerRepository.findByEmail(emailForOtpLookup);
            if (seller == null) {
                throw new ResourceNotFoundException(
                        "No seller account found for this email.");
            }
        }

        // ✅ Google-only guard — if the loaded UserDetails has the
        // sentinel password set by CustomUserServiceImpl for Google-only
        // accounts, reject OTP login immediately with a clear message.
        if ("{noop}GOOGLE_ONLY_NO_OTP".equals(userDetails.getPassword())) {
            throw new GoogleOnlyAccountException(
                    "This account uses Google sign-in. Please sign in with Google instead.");
        }

        VerificationCode verificationCode = latestVerificationCode(emailForOtpLookup);
        validateOtp(verificationCode, otp, emailForOtpLookup);

        verificationCodeRepository.deleteAllByEmail(emailForOtpLookup);

        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }

    // ─────────────────────────────────────────────
    // Shared OTP validation helpers
    // ─────────────────────────────────────────────

    /**
     * Fetches the most recent VerificationCode for an email. Uses
     * findAllByEmailOrderByIdDesc rather than findByEmail so a stray
     * duplicate row (which should no longer be possible once the unique
     * constraint migration has run) degrades to "use the newest one"
     * instead of throwing NonUniqueResultException.
     */
    private VerificationCode latestVerificationCode(String email) {
        return verificationCodeRepository
                .findAllByEmailOrderByIdDesc(email)
                .stream()
                .findFirst()
                .orElse(null);
    }

    /**
     * Validates an OTP against a VerificationCode record: existence,
     * expiry, and match — enforcing a server-side attempt lockout that
     * cannot be bypassed by a client (e.g. direct API calls via Postman)
     * skipping the frontend's own rate limiting.
     */
    private void validateOtp(VerificationCode verificationCode, String otp, String email)
            throws InvalidOtpException, OtpExpiredException, TooManyAttemptsException {

        if (verificationCode == null) {
            throw new InvalidOtpException("No OTP request found for this email. Please request a new OTP.");
        }

        if (verificationCode.getExpiresAt() != null
                && verificationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            verificationCodeRepository.deleteAllByEmail(email);
            log.warn("Expired OTP attempted for: {}", mask(email));
            throw new OtpExpiredException("This OTP has expired. Please request a new one.");
        }

        if (!verificationCode.getOtp().equals(otp)) {
            int attempts = verificationCode.getAttempts() + 1;

            if (attempts >= MAX_OTP_ATTEMPTS) {
                verificationCodeRepository.deleteAllByEmail(email);
                log.warn("OTP attempt lockout triggered for: {}", mask(email));
                throw new TooManyAttemptsException(
                        "Too many failed attempts. Please request a new OTP.");
            }

            verificationCode.setAttempts(attempts);
            verificationCodeRepository.save(verificationCode);

            log.warn("OTP mismatch for: {} ({} attempt(s) remaining)",
                    mask(email), MAX_OTP_ATTEMPTS - attempts);
            throw new InvalidOtpException(
                    "Incorrect OTP. " + (MAX_OTP_ATTEMPTS - attempts) + " attempt(s) remaining.");
        }
    }

    /**
     * Masks an email for logging — e.g. "j***n@gmail.com" — so full
     * addresses don't sit in plaintext application logs.
     */
    private String mask(String email) {
        if (email == null) return null;
        int at = email.indexOf('@');
        if (at <= 1) return "***" + email.substring(Math.max(at, 0));
        return email.charAt(0) + "***" + email.substring(at - 1);
    }
}