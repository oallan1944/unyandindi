package com.allan.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
// IMPORTANT: this unique constraint is what makes duplicate rows for the
// same email impossible going forward. Before this migration runs, ALL
// existing duplicate rows must be deleted first, or Hibernate's ddl-auto
// ALTER TABLE will fail on boot. See the cleanup SQL provided separately.
@Table(name = "verification_code", uniqueConstraints = {
        @UniqueConstraint(name = "uk_verification_code_email", columnNames = "email")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class VerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String otp;

    private String email;

    @OneToOne
    private User user;

    @OneToOne
    private Seller seller;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    // Failed verification attempts against this specific OTP. Used to
    // enforce a server-side lockout independent of any client-side limit,
    // since a client-side-only limit is trivially bypassed via direct API
    // calls (e.g. Postman/curl).
    private int attempts;

}

// package com.allan.model;

// import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.OneToOne;
// import lombok.AllArgsConstructor;
// import lombok.EqualsAndHashCode;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
// import lombok.Setter;

// @Entity
// @Getter
// @Setter
// @AllArgsConstructor
// @NoArgsConstructor
// @EqualsAndHashCode
// public class VerificationCode {

//     @Id
//     @GeneratedValue(strategy = GenerationType.AUTO)
//     private Long id;

//     private String otp;

//     private String email;

//     @OneToOne
//     private User user;

//     @OneToOne
//     private Seller seller;

// }
