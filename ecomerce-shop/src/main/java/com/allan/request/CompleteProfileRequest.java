package com.allan.request;

import lombok.Data;

/**
 * Request body for POST /api/auth/google/complete-profile.
 * Collected from new Google sign-in customers who have not yet
 * provided their mobile number — the minimum required to place
 * an order on Huru Bazar (needed for delivery coordination).
 *
 * fullName is optional here because Google already provided it
 * at account creation — only sent if the customer wants to override
 * the name Google gave us.
 */
@Data
public class CompleteProfileRequest {

    // Optional — overrides the fullName from Google if provided
    private String fullName;

    // Required — Uganda format: 07XXXXXXXX or 03XXXXXXXX (10 digits)
    // Validated in OAuth2UserServiceImpl, not here — keeps the DTO
    // as a plain data carrier with no business logic.
    private String mobile;
}
