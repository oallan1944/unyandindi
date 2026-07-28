package com.allan.security.oauth2;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.allan.config.JwtProvider;
import com.allan.model.User;
import com.allan.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Fires after Google OAuth2 authentication succeeds.
 *
 * <p>Responsibilities:
 * <ol>
 *   <li>Generate a Huru Bazar JWT for the authenticated customer.</li>
 *   <li>Determine the correct frontend redirect target based on
 *       account state.</li>
 *   <li>Pass the JWT as a URL query parameter so the frontend can
 *       store it immediately on landing.</li>
 * </ol>
 *
 * <p><strong>Redirect targets:</strong>
 * <ul>
 *   <li>{@code /complete-profile?token=JWT&newUser=true} —
 *       new Google customer whose profile is incomplete.</li>
 *   <li>{@code /account/settings?googleLinked=true&token=JWT} —
 *       existing LOCAL customer who just auto-linked Google.</li>
 *   <li>{@code /?token=JWT} —
 *       returning Google customer with complete profile.</li>
 * </ul>
 *
 * <p><strong>JWT in URL query parameter:</strong> This is the only
 * viable mechanism to hand a token across a browser redirect boundary.
 * The frontend must move the token from the URL to memory or
 * localStorage immediately via {@code window.history.replaceState()}
 * so it does not persist in browser history or server access logs.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Authentication authentication)
            throws IOException {

        // ── Resolve the authenticated user ───────────────────────
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);

        if (user == null) {
            log.error("OAuth2 success handler: no user found for "
                    + "email {} after authentication", email);
            response.sendRedirect(
                    frontendBaseUrl + "/login?error=user_not_found");
            return;
        }

        // ── Generate JWT ─────────────────────────────────────────
        // Build a full Authentication object with the user's role
        // so JwtProvider can embed it in the token's authorities claim.
        Authentication fullAuth = new UsernamePasswordAuthenticationToken(
                email,
                null,
                List.of(new SimpleGrantedAuthority(
                        user.getRole().toString()))
        );
        String jwt = jwtProvider.generateToken(fullAuth);

        // ── Determine redirect target ────────────────────────────
        String redirectPath = resolveRedirectPath(user);

        String targetUrl = UriComponentsBuilder
                .fromUriString(frontendBaseUrl + redirectPath)
                .queryParam("token", jwt)
                .build()
                .toUriString();

        log.info("OAuth2 success: email={} profileComplete={} "
                + "googleEnabled={} otpEnabled={} redirect={}",
                email,
                user.isProfileComplete(),
                user.isGoogleEnabled(),
                user.isOtpEnabled(),
                redirectPath);

        // ── Clear auth attributes and redirect ───────────────────
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * Resolves the frontend path to redirect to based on account state.
     *
     * <ul>
     *   <li>profileComplete=false → new Google user, needs mobile/name.</li>
     *   <li>googleEnabled=true AND otpEnabled=true AND profileComplete=true
     *       AND primaryAuthProvider=LOCAL → existing LOCAL user who just
     *       auto-linked Google for the first time. Send to settings so
     *       they see the confirmation banner.</li>
     *   <li>All other states → returning Google user, go home.</li>
     * </ul>
     */
    private String resolveRedirectPath(User user) {
        if (!user.isProfileComplete()) {
            // New Google sign-up — must collect mobile number
            return "/complete-profile?newUser=true";
        }

        boolean isAutoLinkedLocalUser =
                user.isGoogleEnabled()
                && user.isOtpEnabled()    // ✅ isOtpEnabled() — boolean field
                && user.isProfileComplete()
                && com.allan.domain.AuthProvider.LOCAL.equals(
                        user.getPrimaryAuthProvider());

        if (isAutoLinkedLocalUser) {
            // LOCAL user who just linked Google — inform them via settings
            return "/account/settings?googleLinked=true";
        }

        // Returning Google user with complete profile — home page
        return "/";
    }
}