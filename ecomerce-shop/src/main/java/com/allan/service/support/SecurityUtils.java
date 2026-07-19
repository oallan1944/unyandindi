package com.allan.service.support;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * The single place identity is resolved from the security context.
 *
 * <p><strong>Security architecture:</strong> every controller in this
 * module MUST call these methods to get {@code userId}/{@code sellerId} —
 * never accept them as a request-body field, path variable, or query
 * param. Centralizing this here means the "never trust the client for
 * identity" rule is enforced in exactly one place instead of re-implemented
 * (and potentially gotten wrong) in every controller method.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Long currentUserId() {
        return principal().getUserId();
    }

    /**
     * @throws IllegalStateException if the current principal has no seller
     *         account — callers on seller-only endpoints should let this
     *         propagate to a 403 via their exception handler, since
     *         reaching a seller endpoint without a seller ID indicates a
     *         misconfigured security rule, not a normal user error.
     */
    public static Long currentSellerId() {
        Long sellerId = principal().getSellerIdOrNull();
        if (sellerId == null) {
            throw new IllegalStateException("Current principal has no associated seller account");
        }
        return sellerId;
    }

    public static boolean isAdmin() {
        return principal().isAdmin();
    }

    private static PlatformPrincipal principal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof PlatformPrincipal p)) {
            throw new IllegalStateException(
                    "No authenticated PlatformPrincipal in security context — is authentication configured "
                            + "and is your UserDetails/JWT principal implementing PlatformPrincipal?");
        }
        return p;
    }
}