

package com.allan.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.allan.domain.USER_ROLE;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "admins")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    private String mobile;

    // Always ROLE_ADMIN — stored in DB for Spring Security
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private USER_ROLE role = USER_ROLE.ROLE_ADMIN;

    // Email verification
    private boolean emailVerified = false;

    // Audit timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Admin-specific: track which sellers this admin has managed
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Coupon> createdCoupons = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Deal> managedDeals = new HashSet<>();

}