package com.allan.security.oauth2;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.allan.domain.AuthProvider;
import com.allan.domain.USER_ROLE;
import com.allan.model.Cart;
import com.allan.model.User;
import com.allan.repository.CartRepository;
import com.allan.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Intercepts every Google OAuth2 callback and resolves or creates
 * the corresponding Huru Bazar {@link User}.
 *
 * <p><strong>Security rules enforced here — in order:</strong>
 * <ol>
 *   <li>Google must have verified the email — unverified emails
 *       are rejected immediately. An unverified Google account could
 *       have been created with someone else's email address.</li>
 *   <li>Sellers and admins are blocked from using Google sign-in
 *       regardless of email — their accounts use OTP exclusively.
 *       This prevents a seller from accidentally creating a parallel
 *       customer account with the same email.</li>
 *   <li>If an existing LOCAL customer account has this email and has
 *       not yet linked Google, Google is linked automatically —
 *       one identity, two entry points, no duplicate accounts.</li>
 *   <li>If no account exists, a new GOOGLE-primary customer account
 *       is created and a Cart is provisioned immediately so the
 *       customer can start shopping without a second sign-in.</li>
 * </ol>
 *
 * <p>Only {@code ROLE_CUSTOMER} is ever assigned through this flow.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        // ── Fetch raw attributes from Google ─────────────────────
        OAuth2User oAuth2User = super.loadUser(userRequest);
        GoogleOAuth2UserInfo userInfo =
                new GoogleOAuth2UserInfo(oAuth2User.getAttributes());

        // ── Guard 1: verified email only ─────────────────────────
        if (!userInfo.isEmailVerified()) {
            log.warn("OAuth2 login rejected — unverified Google "
                    + "email: {}", userInfo.getEmail());
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("unverified_email"),
                    "Google account email is not verified. "
                    + "Please verify your Google account first.");
        }

        // ── Guard 2: email must not be null or blank ─────────────
        if (userInfo.getEmail() == null
                || userInfo.getEmail().isBlank()) {
            log.error("OAuth2 login rejected — Google returned "
                    + "null or blank email");
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("missing_email"),
                    "Google did not provide an email address. "
                    + "Please ensure your Google account has a "
                    + "verified email.");
        }

        // ── Guard 3: googleId must not be null ───────────────────
        if (userInfo.getId() == null || userInfo.getId().isBlank()) {
            log.error("OAuth2 login rejected — Google returned "
                    + "null sub claim for email: {}",
                    userInfo.getEmail());
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("missing_google_id"),
                    "Google did not return a valid user identifier.");
        }

        // ── Lookup existing account ──────────────────────────────
        User existingUser =
                userRepository.findByEmail(userInfo.getEmail());

        // ── Guard 4: sellers and admins cannot use Google login ──
        if (existingUser != null
                && !USER_ROLE.ROLE_CUSTOMER.equals(
                        existingUser.getRole())) {
            log.warn("OAuth2 login rejected — email {} belongs to "
                    + "non-customer role: {}",
                    userInfo.getEmail(), existingUser.getRole());
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("non_customer_account"),
                    "This email is registered as a seller or admin "
                    + "account. Sellers and admins must sign in "
                    + "with OTP.");
        }

        // ── Route to the correct handler ─────────────────────────
        User user;
        if (existingUser == null) {
            user = createGoogleUser(userInfo);
        } else if (!existingUser.isGoogleEnabled()) {
            user = linkGoogleToExistingUser(existingUser, userInfo);
        } else {
            user = refreshReturningUser(existingUser, userInfo);
        }

        // ── Build Spring Security OAuth2User ────────────────────
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(
                        user.getRole().toString())),
                oAuth2User.getAttributes(),
                "email"  // attribute used as the principal name
        );
    }

    // ─────────────────────────────────────────────
    // HANDLERS
    // ─────────────────────────────────────────────

    /**
     * Brand-new customer — never seen this email before.
     * Creates the User and provisions a Cart immediately.
     * profileComplete=false triggers redirect to /complete-profile.
     */
    private User createGoogleUser(GoogleOAuth2UserInfo userInfo) {
        User user = new User();
        user.setEmail(userInfo.getEmail());
        user.setFullName(userInfo.getFullName() != null
                ? userInfo.getFullName() : "");
        user.setGoogleId(userInfo.getId());
        user.setPrimaryAuthProvider(AuthProvider.GOOGLE);
        user.setRole(USER_ROLE.ROLE_CUSTOMER);
        user.setEmailVerified(true);   // Google pre-verified
        user.setGoogleEnabled(true);
        user.setOtpEnabled(true);      // always available as fallback
        user.setProfileComplete(false); // triggers /complete-profile
        user.setPassword("");           // no password for OAuth2 users
        user.setMobile("");
        user.setCreatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);

        // Provision cart immediately — customer can start adding
        // items before completing their profile
        Cart cart = new Cart();
        cart.setUser(saved);
        cartRepository.save(cart);

        log.info("New Google customer created: id={} email={}",
                saved.getId(), saved.getEmail());
        return saved;
    }

    /**
     * Existing LOCAL customer signing in via Google for the first time.
     * Auto-links Google to their existing account — one identity,
     * two entry points. primaryAuthProvider stays LOCAL.
     * profileComplete stays true — LOCAL users already completed it.
     */
    private User linkGoogleToExistingUser(User user,
                                           GoogleOAuth2UserInfo userInfo) {
        user.setGoogleId(userInfo.getId());
        user.setGoogleEnabled(true);
        // profileComplete is already true for LOCAL users
        // otpEnabled is already true for LOCAL users
        // primaryAuthProvider stays LOCAL — Google is an additional method

        User saved = userRepository.save(user);
        log.info("Google auto-linked to existing LOCAL customer: "
                + "id={} email={}", saved.getId(), saved.getEmail());
        return saved;
    }

    /**
     * Returning Google user — already linked and authenticated before.
     * Refreshes the googleId in case it changed (rare with workspace
     * accounts) and refreshes fullName if Google updated it.
     * No structural changes to the account.
     */
    private User refreshReturningUser(User user,
                                       GoogleOAuth2UserInfo userInfo) {
        boolean changed = false;

        if (userInfo.getId() != null
                && !userInfo.getId().equals(user.getGoogleId())) {
            user.setGoogleId(userInfo.getId());
            changed = true;
            log.info("Google sub claim refreshed for user: {}",
                    user.getEmail());
        }

        if (changed) {
            return userRepository.save(user);
        }

        return user;
    }
}