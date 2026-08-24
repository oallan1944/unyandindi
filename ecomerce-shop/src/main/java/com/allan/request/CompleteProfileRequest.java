package com.allan.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for PATCH /users/complete-profile.
 *
 * Used to finish setting up an account created via Google OAuth2,
 * where CustomOAuth2UserService leaves fullName possibly blank and
 * mobile always blank (Google doesn't provide a phone number).
 *
 * @Valid on the controller method triggers these constraints — a
 * request with a blank name or a malformed mobile number is rejected
 * with a 400 before it ever reaches the service layer.
 */
@Data
public class CompleteProfileRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be exactly 10 digits")
    private String mobile;
}