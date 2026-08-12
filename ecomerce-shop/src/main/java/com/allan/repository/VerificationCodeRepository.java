package com.allan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.allan.model.VerificationCode;
import java.util.Optional;
import java.util.List;


public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional <VerificationCode> findByEmail(String email);

    // Use this to read the "latest" code when duplicates might exist,
    // instead of findByEmail (which throws NonUniqueResultException on >1 row).
    List<VerificationCode> findAllByEmailOrderByIdDesc(String email);

    VerificationCode findByOtp(String otp);

    // Bulk delete — cannot throw on multiple matching rows, unlike
    // find-then-delete. Use this before issuing a new OTP.
    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationCode v WHERE v.email = :email")
    void deleteAllByEmail(@Param("email") String email);

}