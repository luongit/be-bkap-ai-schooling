package com.bkap.aispark.service.refresh_tokens;

import com.bkap.aispark.entity.User;
import com.bkap.aispark.entity.refresh_tokens.RefreshToken;
import com.bkap.aispark.repository.refresh_tokens.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    // Tao refresh token moi cho user
    public RefreshToken createToken(User user, String tokenValue, long daysToLive) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(tokenValue);
        token.setExpiredAt(LocalDateTime.now().plusDays(daysToLive));
        token.setRevoked(false);

        return refreshTokenRepository.save(token);
    }

    // Tim refresh token theo gia tri token
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    // Kiem tra refresh token con hop le khong
    public boolean isValid(RefreshToken token) {
        return !token.getRevoked() && token.getExpiredAt().isAfter(LocalDateTime.now());
    }

    // Thu hoi token
    public void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    // Khi logout xoa toan bo refresh token cua user
    public void revokeAllForUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}