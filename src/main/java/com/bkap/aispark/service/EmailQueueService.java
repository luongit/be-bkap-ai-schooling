package com.bkap.aispark.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.bkap.aispark.entity.EmailQueue;
import com.bkap.aispark.entity.EmailStatus;
import com.bkap.aispark.repository.EmailQueueRepository;

@Service
public class EmailQueueService {

    private final EmailQueueRepository emailQueueRepository;

    public EmailQueueService(EmailQueueRepository emailQueueRepository) {
        this.emailQueueRepository = emailQueueRepository;
    }

    // Queue một email; trả về entity đã save
    public EmailQueue queueWelcomeEmail(String recipient, String subject, String content) {
        EmailQueue q = new EmailQueue(recipient, subject, content, EmailStatus.PENDING);
        q.setCreatedAt(LocalDateTime.now());
        return emailQueueRepository.save(q);
    }
}
