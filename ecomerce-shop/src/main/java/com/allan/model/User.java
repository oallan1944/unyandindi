package com.allan.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.allan.domain.AuthProvider;
import com.allan.domain.USER_ROLE;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

// ✅ EqualsAndHashCode on id only — bare @EqualsAndHashCode includes all
// fields which causes a well-known JPA proxy equality bug: Hibernate proxies
// have uninitialized fields at comparison time, making two references to the
// same DB row appear unequal. id-only comparison is always correct for entities.
@EqualsAndHashCode(onlyExplicitlyIncluded = true)

// ✅ Explicit table name — prevents Hibernate from using the class name
// which can conflict across schemas or change if the class is renamed.
// Indexes on email and googleId — both are queried on every auth request.
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_user_email",     columnList = "email"),
        @Index(name = "idx_user_google_id", columnList = "googleId")
    }
)
public class User {

    // ─────────────────────────────────────────────
    // IDENTITY
    // ─────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // ✅ IDENTITY not AUTO — AUTO delegates to a sequence table in some
    // databases and causes unexpected behaviour with Hibernate batch inserts.
    // IDENTITY is explicit and consistent with your Order entity.
    @EqualsAndHashCode.Include
    private Long id;

    // ✅ unique + nullable=false — two users with the same email must never
    // exist. The DB constraint is the final safety net even if service-layer
    // checks are bypassed by a race condition.
    @Column(unique = true, nullable = false)
    private String email;

    private String fullName;

    private String mobile;

    // ─────────────────────────────────────────────
    // AUTHENTICATION
    // ─────────────────────────────────────────────

    // ✅ WRITE_ONLY — password hash is never serialized in any API response.
    // Empty string ("") for Google-primary users who have no password.
    // Never null — null would cause NullPointerException in password
    // comparison inside CustomUserServiceImpl.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password = "";

    // ✅ EnumType.STRING — stores "LOCAL" or "GOOGLE" as a readable string.
    // EnumType.ORDINAL is fragile — inserting a new enum value in the middle
    // silently shifts all existing ordinal values and corrupts every row.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider primaryAuthProvider = AuthProvider.LOCAL;

    // ✅ @JsonIgnore — googleId is an internal identity token from Google.
    // Exposing it in API responses would allow an attacker who intercepts
    // a response to attempt to impersonate the user on other systems.
    // unique = true — prevents two Huru Bazar accounts from linking the
    // same Google identity, which would allow one Google account to access
    // two customer profiles.
    @JsonIgnore
    @Column(unique = true)
    private String googleId;

    // ✅ googleEnabled — tracks whether Google OAuth2 is an active sign-in
    // method for this account. Separate from googleId being non-null because
    // an admin could suspend Google access without removing the googleId record.
    private boolean googleEnabled = false;

    // ✅ otpEnabled — tracks whether OTP email sign-in is active.
    // Always true for LOCAL accounts. Used as the lockout-prevention guard:
    // unlinking Google is blocked when otpEnabled=false so the customer
    // always retains at least one working sign-in method.
    private boolean otpEnabled = true;

    // ─────────────────────────────────────────────
    // ROLE & VERIFICATION
    // ─────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private USER_ROLE role = USER_ROLE.ROLE_CUSTOMER;

    // ✅ emailVerified — true for all Google sign-ins (Google pre-verifies)
    // and for LOCAL users after OTP confirmation.
    private boolean emailVerified = false;

    // ✅ profileComplete — false for new Google sign-ins until the customer
    // fills in mobile + name on /complete-profile. Always true for LOCAL
    // users (they complete profile during OTP signup flow). Shopping flows
    // (cart, checkout, orders) are gated behind this being true on the frontend.
    private boolean profileComplete = false;

    // ─────────────────────────────────────────────
    // AUDIT
    // ─────────────────────────────────────────────

    private LocalDateTime createdAt;

    // ✅ updatedAt — not in the original but needed for the account-linking
    // flow: when a user links or unlinks Google, updatedAt records when
    // the change happened. Useful for support tickets ("when did this account
    // get linked?") and security auditing.
    private LocalDateTime updatedAt;

    // ─────────────────────────────────────────────
    // RELATIONSHIPS
    // ─────────────────────────────────────────────

    // ✅ FetchType.LAZY — addresses are never needed when serializing a User
    // in auth responses, order summaries, or seller dashboards. EAGER would
    // load them on every User fetch regardless of whether they're needed,
    // adding unnecessary DB queries on every auth call.
    // @JsonIgnore — prevents LazyInitializationException during JSON
    // serialization outside of a transaction (the bug you hit earlier
    // with User.addresses causing 500 on GET /api/admin/orders).
    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY)
    private Set<Address> addresses = new HashSet<>();

    // ✅ @JsonIgnore — usedCoupons is a large join table. Exposing it in
    // responses would leak which promotions this customer has used —
    // a privacy concern — and would load potentially hundreds of rows
    // on every User serialization.
    // Your new CouponRedemption entity supersedes this relationship
    // for the coupon module — this field is kept for backward compatibility
    // with existing cart/order flows that reference it, but new coupon
    // logic should use CouponRedemptionRepository instead.
    @JsonIgnore
    @ManyToMany
    private Set<Coupon> usedCoupons = new HashSet<>();
}