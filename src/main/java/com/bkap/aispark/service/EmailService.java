package com.bkap.aispark.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine; // để render Thymeleaf

    public void sendOtpEmail(String to, String otp) {
        try {
            // Tạo context Thymeleaf
            Context context = new Context();
            context.setVariable("otp", otp);

            // Render file HTML (otp-email.html trong resources/templates)
            String htmlContent = templateEngine.process("otp-email", context);

            // Tạo MimeMessage
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Mã OTP đặt lại mật khẩu");
            helper.setText(htmlContent, true); // ⚡ true = gửi HTML

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Lỗi gửi email", e);
        }
    }
}
