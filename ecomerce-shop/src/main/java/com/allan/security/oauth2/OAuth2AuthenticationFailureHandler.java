package com.allan.security.oauth2;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Fires when Google OAuth2 authentication fails for any reason:
 * <ul>
 *   <li>Google rejected the login (user cancelled, account suspended).</li>
 *   <li>{@link CustomOAuth2UserService} threw an
 *       {@code OAuth2AuthenticationException} (unverified email,
 *       non-customer account, missing email/googleId).</li>
 *   <li>Network error reaching Google's token or userinfo endpoints.</li>
 * </ul>
 *
 * <p>Redirects to the frontend login page with a typed error code
 * query parameter so the UI can display a meaningful, actionable
 * message rather than a blank screen or a generic "login failed."
 *
 * <p><strong>Error codes passed to the frontend:</strong>
 * <ul>
 *   <li>{@code provider_mismatch} — email registered under a different
 *       provider (currently unused — auto-link handles this instead).</li>
 *   <li>{@code unverified_email} — Google account email not verified.</li>
 *   <li>{@code non_customer_account} — email belongs to a seller/admin.</li>
 *   <li>{@code missing_email} — Google did not return an email.</li>
 *   <li>{@code missing_google_id} — Google did not return a sub claim.</li>
 *   <li>{@code oauth2_error} — catch-all for any other failure.</li>
 * </ul>
 */
@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler
        extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                         HttpServletResponse response,
                                         AuthenticationException exception)
            throws IOException {

        String errorCode = extractErrorCode(exception.getMessage());

        String targetUrl = UriComponentsBuilder
                .fromUriString(frontendBaseUrl + "/login")
                .queryParam("error", errorCode)
                .build()
                .toUriString();

        log.warn("OAuth2 authentication failed: errorCode={} "
                + "message={}", errorCode, exception.getMessage());

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * Maps the exception message to a typed error code.
     * Error codes are stable string constants — the frontend
     * maps them to localised user-facing messages.
     * New error types from CustomOAuth2UserService must be
     * added here to avoid falling through to the catch-all.
     */
    private String extractErrorCode(String message) {
        if (message == null || message.isBlank()) {
            return "oauth2_error";
        }
        if (message.contains("unverified_email")) {
            return "unverified_email";
        }
        if (message.contains("non_customer_account")) {
            return "non_customer_account";
        }
        if (message.contains("missing_email")) {
            return "missing_email";
        }
        if (message.contains("missing_google_id")) {
            return "missing_google_id";
        }
        if (message.contains("provider_mismatch")) {
            return "provider_mismatch";
        }
        if (message.contains("user_not_found")) {
            return "user_not_found";
        }
        return "oauth2_error";
    }
}