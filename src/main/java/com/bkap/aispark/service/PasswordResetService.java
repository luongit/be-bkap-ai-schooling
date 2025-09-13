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


    @Transactional
    public String createPasswordResetToken(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống"));

        // Sinh OTP 6 số
        String otp = String.format("%06d", new Random().nextInt(900000) + 100000);

        // OTP sống 3 phút
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(3);

        // Xóa OTP cũ nếu có
        tokenRepo.deleteByUserId(user.getId());

        PasswordResetToken resetToken = new PasswordResetToken(otp, user.getId(), expiry);
        tokenRepo.save(resetToken);

        // Gửi email OTP
        emailService.sendOtpEmail(user.getEmail(), otp);

        System.out.println("OTP đã gửi: " + otp); // Log để debug
        return otp; // ⚠️ trả về OTP cho test
    }

    @Transactional
    public void resetPassword(String otp, String newPassword) {
        otp = otp.trim();
        PasswordResetToken resetToken = tokenRepo.findByToken(otp)
                .orElseThrow(() -> new RuntimeException("OTP không hợp lệ"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP đã hết hạn");
        }

        User user = userRepo.findById(resetToken.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        tokenRepo.deleteByToken(otp);
    }
}
