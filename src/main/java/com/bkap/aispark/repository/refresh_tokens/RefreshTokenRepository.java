package com.bkap.aispark.repository.refresh_tokens;

import com.bkap.aispark.entity.User;
import com.bkap.aispark.entity.refresh_tokens.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}