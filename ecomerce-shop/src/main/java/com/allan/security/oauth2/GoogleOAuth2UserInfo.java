package com.allan.security.oauth2;

import java.util.Map;

/**
 * Typed accessor for Google's OAuth2 attribute map.
 *
 * Isolates all knowledge of Google's raw attribute key names
 * ("sub", "email", "given_name", etc.) in one place so nothing
 * else in the codebase depends on raw string keys.
 *
 * Google attribute reference:
 * https://developers.google.com/identity/openid-connect/openid-connect#obtainuserinfo
 */
public class GoogleOAuth2UserInfo {

    private final Map<String, Object> attributes;

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    /**
     * Google's unique user identifier (the "sub" claim).
     * Stable across email changes — this is the authoritative
     * identity token, not the email address.
     */
    public String getId() {
        return (String) attributes.get("sub");
    }

    public String getEmail() {
        return (String) attributes.get("email");
    }

    public String getFullName() {
        return (String) attributes.get("name");
    }

    public String getFirstName() {
        return (String) attributes.get("given_name");
    }

    public String getLastName() {
        return (String) attributes.get("family_name");
    }

    public String getPictureUrl() {
        return (String) attributes.get("picture");
    }

    /**
     * Whether Google has verified this email address.
     * Always check this before trusting the email — unverified
     * emails must be rejected to prevent account takeover via
     * an unverified Google account with a spoofed email address.
     */
    public boolean isEmailVerified() {
        Object verified = attributes.get("email_verified");
        if (verified instanceof Boolean) {
            return (Boolean) verified;
        }
        // Google sometimes returns this as a String "true"/"false"
        if (verified instanceof String) {
            return Boolean.parseBoolean((String) verified);
        }
        return false;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}