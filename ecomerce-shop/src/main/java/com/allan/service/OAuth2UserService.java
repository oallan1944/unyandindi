package com.allan.service;

import com.allan.request.CompleteProfileRequest;
import com.allan.model.User;
import com.allan.response.AuthMethodsStatus;

public interface OAuth2UserService {

    User completeProfile(String email,
                         CompleteProfileRequest request) throws Exception;

    boolean isProfileComplete(String email) throws Exception;

    /**
     * Explicitly links a Google account to an existing LOCAL account
     * from the profile settings page (user-initiated, not auto-triggered).
     * Auto-linking already happens in CustomOAuth2UserService on first
     * Google sign-in — this endpoint is for users who want to link
     * proactively before ever trying Google login.
     */
    User linkGoogleAccount(String email, String googleId) throws Exception;

    /**
     * Unlinks Google from an account.
     * Blocked if otpEnabled=false — prevents total lockout.
     */
    User unlinkGoogleAccount(String email) throws Exception;

    /**
     * Returns a summary of which auth methods are active for this account.
     * Used by the frontend account settings page to show the correct
     * "Link Google" / "Unlink Google" / "Set up OTP" buttons.
     */
    AuthMethodsStatus getAuthMethodsStatus(String email) throws Exception;
}