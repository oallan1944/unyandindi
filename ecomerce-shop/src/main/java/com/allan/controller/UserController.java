package com.allan.controller;

import com.allan.dto.UserSummaryDTO;
import com.allan.model.User;
import com.allan.request.CompleteProfileRequest;
import com.allan.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<User> userProfileHandler(
            @RequestHeader(value = "Authorization", required = false) String jwt) {

        // ✅ guard: missing header
        if (jwt == null || jwt.isBlank()) {
            log.debug("Profile request received with no Authorization header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // ✅ guard: malformed header
        if (!jwt.startsWith("Bearer ") || jwt.length() <= 7) {
            log.warn("Malformed Authorization header: does not start with 'Bearer ' or token is empty");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            User user = userService.findUserByJwtToken(jwt);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Failed to fetch user profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Finishes account setup for a customer whose profile is incomplete
     * — currently only reachable this way for new Google OAuth2 signups
     * (see CustomOAuth2UserService.createGoogleUser, which sets
     * profileComplete=false and mobile="").
     *
     * Auth is enforced manually here, identically to userProfileHandler
     * above, because this path currently falls under SecurityConfig's
     * `.anyRequest().permitAll()` catch-all rather than an explicit
     * `.authenticated()` matcher. If/when that's tightened at the
     * SecurityConfig layer, this manual check remains valid as a
     * second layer of defense — it doesn't need to be removed.
     *
     * Returns UserSummaryDTO rather than the full User entity — no
     * reason for the frontend to receive password/googleId/addresses
     * fields (even though those already carry @JsonIgnore) when this
     * response only ever needs to update the four summary fields.
     */
    @PatchMapping("/complete-profile")
    public ResponseEntity<UserSummaryDTO> completeProfileHandler(
            @RequestHeader(value = "Authorization", required = false) String jwt,
            @Valid @RequestBody CompleteProfileRequest request) {

        if (jwt == null || jwt.isBlank()) {
            log.debug("Complete-profile request received with no Authorization header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!jwt.startsWith("Bearer ") || jwt.length() <= 7) {
            log.warn("Malformed Authorization header on complete-profile request");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            User user = userService.findUserByJwtToken(jwt);
            UserSummaryDTO updated = userService.completeProfile(user, request);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Failed to complete profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}