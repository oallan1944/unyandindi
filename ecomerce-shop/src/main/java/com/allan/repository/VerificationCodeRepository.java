package com.allan.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.allan.model.VerificationCode;
import java.util.Optional;


public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional <VerificationCode> findByEmail(String email);

    VerificationCode findByOtp(String otp);

}
