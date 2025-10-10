package com.bkap.aispark.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.bkap.aispark.entity.EmailQueue;
import com.bkap.aispark.entity.EmailStatus;
import com.bkap.aispark.repository.EmailQueueRepository;

@Service
public class EmailSenderService {

    @Autowired
    private EmailQueueRepository emailQueueRepo;

    @Autowired
    private JavaMailSender mailSender;

    // Worker chạy mỗi 1 phút
    @Scheduled(fixedDelay = 60000)
    public void processPendingEmails() {
        List<EmailQueue> pending = emailQueueRepo.findByStatus(EmailStatus.PENDING);

        for (EmailQueue email : pending) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email.getRecipient());
                message.setSubject(email.getSubject());
                message.setText(email.getContent());

                mailSender.send(message);

                email.setStatus(EmailStatus.SENT);
                email.setSentAt(LocalDateTime.now());
            } catch (Exception e) {
                email.setStatus(EmailStatus.FAILED);
            }
            emailQueueRepo.save(email);
        }
    }
}
