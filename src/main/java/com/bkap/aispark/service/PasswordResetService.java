package com.bkap.aispark.service;

import com.bkap.aispark.entity.PasswordResetToken;
import com.bkap.aispark.entity.User;
import com.bkap.aispark.repository.PasswordResetTokenRepository;
import com.bkap.aispark.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository tokenRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;

    /**
     * Sinh OTP và gửi về email (HTML template)
     */
    @Transactional
    public String createPasswordResetToken(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống"));

        // Sinh OTP 6 số
        String otp = String.format("%06d", new Random().nextInt(900000) + 100000);

        LocalDateTime expiry = LocalDateTime.now().plusMinutes(1); // OTP sống 1 phút

        // Xóa OTP cũ nếu có rồi lưu OTP mới
        tokenRepo.deleteByUserId(user.getId());
        PasswordResetToken resetToken = new PasswordResetToken(otp, user.getId(), expiry);
        tokenRepo.save(resetToken);

        // ✅ Gửi email OTP bằng HTML
        emailService.sendOtpEmail(user.getEmail(), otp);

        return otp; // để test Thunder, production thì bỏ return
    }

    /**
     * Đổi mật khẩu bằng OTP
     */
    @Transactional
    public boolean resetPassword(String otp, String newPassword) {
        PasswordResetToken resetToken = tokenRepo.findByToken(otp)
                .orElseThrow(() -> new RuntimeException("OTP không hợp lệ"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP đã hết hạn");
        }

        User user = userRepo.findById(resetToken.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        // Xóa OTP đã dùng
        tokenRepo.deleteByToken(otp);

        return true;
    }
}
