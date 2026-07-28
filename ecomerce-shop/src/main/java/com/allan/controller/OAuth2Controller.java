package com.allan.controller;

import com.allan.request.CompleteProfileRequest;
import com.allan.response.AuthMethodsStatus;
import com.allan.model.User;
import com.allan.service.OAuth2UserService;
import com.allan.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/google")
public class OAuth2Controller {

    private final OAuth2UserService oAuth2UserService;
    private final UserService userService;

    // ── Profile completion ────────────────────────────────────────

    @PostMapping("/complete-profile")
    public ResponseEntity<?> completeProfile(
            @RequestHeader(value = "Authorization",
                           required = false) String jwt,
            @RequestBody CompleteProfileRequest request) throws Exception {

        if (isInvalidJwt(jwt)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error",
                            "Valid Authorization header required."));
        }

        User user = userService.findUserByJwtToken(jwt);
        User updated = oAuth2UserService.completeProfile(
                user.getEmail(), request);

        return ResponseEntity.ok(Map.of(
                "message", "Profile completed successfully.",
                "profileComplete", updated.isProfileComplete(),
                "email", updated.getEmail(),
                "fullName", updated.getFullName(),
                "mobile", updated.getMobile()
        ));
    }

    @GetMapping("/profile-status")
    public ResponseEntity<?> getProfileStatus(
            @RequestHeader(value = "Authorization",
                           required = false) String jwt) throws Exception {

        if (isInvalidJwt(jwt)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error",
                            "Valid Authorization header required."));
        }

        User user = userService.findUserByJwtToken(jwt);
        boolean complete = oAuth2UserService.isProfileComplete(
                user.getEmail());

        return ResponseEntity.ok(Map.of(
                "profileComplete", complete,
                "email", user.getEmail()
        ));
    }

    // ── Auth methods status ───────────────────────────────────────

    /**
     * Returns which sign-in methods are active for this account.
     * Used by the account settings page to show the correct
     * "Link Google" / "Unlink Google" buttons.
     */
    @GetMapping("/auth-methods")
    public ResponseEntity<AuthMethodsStatus> getAuthMethods(
            @RequestHeader(value = "Authorization",
                           required = false) String jwt) throws Exception {

        if (isInvalidJwt(jwt)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userService.findUserByJwtToken(jwt);
        AuthMethodsStatus status = oAuth2UserService
                .getAuthMethodsStatus(user.getEmail());
        return ResponseEntity.ok(status);
    }

    // ── Account linking ───────────────────────────────────────────

    /**
     * User-initiated Google linking from account settings.
     * Requires a googleId obtained from the frontend after a
     * silent Google auth (Google One Tap or similar).
     * Auto-linking already handles the case where the user
     * signs in via Google for the first time.
     */
    @PostMapping("/link")
    public ResponseEntity<?> linkGoogleAccount(
            @RequestHeader(value = "Authorization",
                           required = false) String jwt,
            @RequestBody Map<String, String> body) throws Exception {

        if (isInvalidJwt(jwt)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error",
                            "Valid Authorization header required."));
        }

        String googleId = body.get("googleId");
        if (googleId == null || googleId.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "googleId is required."));
        }

        User user = userService.findUserByJwtToken(jwt);
        oAuth2UserService.linkGoogleAccount(user.getEmail(), googleId);

        log.info("Google account linked via settings: {}", user.getEmail());
        return ResponseEntity.ok(Map.of(
                "message", "Google account linked successfully.",
                "googleEnabled", true
        ));
    }

    /**
     * Unlinks Google from the account.
     * Blocked if OTP is not enabled — prevents lockout.
     */
    @DeleteMapping("/unlink")
    public ResponseEntity<?> unlinkGoogleAccount(
            @RequestHeader(value = "Authorization",
                           required = false) String jwt) throws Exception {

        if (isInvalidJwt(jwt)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error",
                            "Valid Authorization header required."));
        }

        User user = userService.findUserByJwtToken(jwt);
        oAuth2UserService.unlinkGoogleAccount(user.getEmail());

        log.info("Google account unlinked: {}", user.getEmail());
        return ResponseEntity.ok(Map.of(
                "message", "Google account unlinked successfully.",
                "googleEnabled", false
        ));
    }

    // ── Shared guard ──────────────────────────────────────────────

    private boolean isInvalidJwt(String jwt) {
        return jwt == null
                || jwt.isBlank()
                || !jwt.startsWith("Bearer ")
                || jwt.length() <= 7;
    }
}