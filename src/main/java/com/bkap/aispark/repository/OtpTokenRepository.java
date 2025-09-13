package com.bkap.aispark.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bkap.aispark.entity.OtpToken;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findByEmailAndOtp(String email, String otp);
    void deleteByEmail(String email);
}


