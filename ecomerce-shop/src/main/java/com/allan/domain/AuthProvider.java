package com.allan.domain;

/**
 * Identifies how a User account was originally created and which
 * authentication mechanisms are valid for it.
 *
 * LOCAL  — account created via OTP email flow. Password hash is set.
 *          Google may be linked later but PRIMARY stays LOCAL.
 * GOOGLE — account created via Google OAuth2. Password is empty string.
 *          OTP is always available as a fallback since email is verified
 *          by Google at creation time.
 */
public enum AuthProvider {
    LOCAL,
    GOOGLE
}