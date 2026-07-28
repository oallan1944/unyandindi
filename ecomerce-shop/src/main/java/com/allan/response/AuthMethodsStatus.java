package com.allan.response;

import lombok.Data;

/**
 * Tells the frontend which authentication methods are
 * currently active for this account.
 * Used on the account settings page to render the correct
 * link/unlink/enable buttons.
 */
@Data
public class AuthMethodsStatus {
    private boolean otpEnabled;
    private boolean googleEnabled;
    private String primaryAuthProvider;
    private boolean profileComplete;
    private boolean canUnlinkGoogle;
    private String email;
}