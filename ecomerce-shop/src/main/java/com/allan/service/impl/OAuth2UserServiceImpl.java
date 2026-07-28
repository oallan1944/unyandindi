package com.allan.service.impl;

import com.allan.request.CompleteProfileRequest;
import com.allan.response.AuthMethodsStatus;
import com.allan.model.User;
import com.allan.repository.UserRepository;
import com.allan.service.OAuth2UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserServiceImpl implements OAuth2UserService {

    private final UserRepository userRepository;

    // ─────────────────────────────────────────────
    // PROFILE COMPLETION
    // ─────────────────────────────────────────────

    @Override
    @Transactional
    public User completeProfile(String email,
                                 CompleteProfileRequest request)
            throws Exception {
        User user = findUser(email);

        if (request.getMobile() == null
                || request.getMobile().isBlank()) {
            throw new Exception("Mobile number is required.");
        }
        // Uganda mobile format: 07XXXXXXXX or 03XXXXXXXX
        if (!request.getMobile().matches("^(07|03)\\d{8}$")) {
            throw new Exception(
                    "Invalid mobile number. Must be a valid Uganda "
                    + "number starting with 07 or 03 (10 digits total).");
        }

        if (request.getFullName() != null
                && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        user.setMobile(request.getMobile());
        user.setProfileComplete(true);

        User updated = userRepository.save(user);
        log.info("Profile completed for Google user: {}", email);
        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProfileComplete(String email) throws Exception {
        return findUser(email).isProfileComplete();
    }

    // ─────────────────────────────────────────────
    // ACCOUNT LINKING
    // ─────────────────────────────────────────────

    @Override
    @Transactional
    public User linkGoogleAccount(String email,
                                   String googleId) throws Exception {
        User user = findUser(email);

        if (user.isGoogleEnabled()) {
            throw new Exception(
                    "Google account is already linked to this account.");
        }

        // Prevent the same Google identity linking to two accounts
        User existingGoogleUser =
                userRepository.findByGoogleId(googleId);
        if (existingGoogleUser != null
                && !existingGoogleUser.getEmail().equals(email)) {
            throw new Exception(
                    "This Google account is already linked to a "
                    + "different Huru Bazar account.");
        }

        user.setGoogleId(googleId);
        user.setGoogleEnabled(true);

        User updated = userRepository.save(user);
        log.info("Google account linked to user: {}", email);
        return updated;
    }

    @Override
    @Transactional
    public User unlinkGoogleAccount(String email) throws Exception {
        User user = findUser(email);

        if (!user.isGoogleEnabled()) {
            throw new Exception(
                    "Google account is not linked to this account.");
        }

        // Lockout prevention — must have at least one other auth method
        if (!user.isOtpEnabled()) {  // ✅ isOtpEnabled() not getOtpEnabled()
            throw new Exception(
                    "Cannot unlink Google — OTP sign-in is not enabled. "
                    + "Please verify your email for OTP access first, "
                    + "then unlink Google.");
        }

        user.setGoogleId(null);
        user.setGoogleEnabled(false);

        User updated = userRepository.save(user);
        log.info("Google account unlinked from user: {}", email);
        return updated;
    }

    // ─────────────────────────────────────────────
    // AUTH METHODS STATUS
    // ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AuthMethodsStatus getAuthMethodsStatus(String email)
            throws Exception {
        User user = findUser(email);

        AuthMethodsStatus status = new AuthMethodsStatus();
        status.setEmail(user.getEmail());
        status.setOtpEnabled(user.isOtpEnabled());        // ✅ isOtpEnabled()
        status.setGoogleEnabled(user.isGoogleEnabled());  // ✅ isGoogleEnabled()
        status.setPrimaryAuthProvider(
                user.getPrimaryAuthProvider().toString());
        status.setProfileComplete(user.isProfileComplete());
        // canUnlinkGoogle = Google linked AND OTP available as fallback
        status.setCanUnlinkGoogle(
                user.isGoogleEnabled() && user.isOtpEnabled());
        return status;
    }

    // ─────────────────────────────────────────────
    // PRIVATE HELPER
    // ─────────────────────────────────────────────

    private User findUser(String email) throws Exception {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new Exception("User not found for email: " + email);
        }
        return user;
    }
}