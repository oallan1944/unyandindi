package com.allan.service.support;

/**
 * Contract your authenticated principal (custom {@code UserDetails}
 * implementation, or a JWT-claims-backed principal) must satisfy for
 * {@link SecurityUtils} to extract IDs safely.
 *
 * <p><strong>Integration point:</strong> this project's actual
 * authentication principal class wasn't provided, so this interface is the
 * seam — have your existing principal/UserDetails class implement it (or
 * adapt a JWT converter to produce one). Every controller in this module
 * that needs {@code userId} or {@code sellerId} goes through
 * {@link SecurityUtils}, never through a request body field, specifically
 * so this is the ONLY place identity resolution logic lives.
 */
public interface PlatformPrincipal {

    /** The authenticated user's ID — always present for any authenticated request. */
    Long getUserId();

    /**
     * The authenticated user's seller ID if they hold a seller/vendor
     * account, or {@code null} if they're a customer-only or admin
     * principal without seller privileges.
     */
    Long getSellerIdOrNull();

    boolean isAdmin();
}