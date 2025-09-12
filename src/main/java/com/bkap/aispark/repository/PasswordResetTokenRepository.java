package com.bkap.aispark.repository;

import com.bkap.aispark.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    @Transactional
    void deleteByToken(String token);

    @Transactional
    void deleteByUserId(Long userId);
}

