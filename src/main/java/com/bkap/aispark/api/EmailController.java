package com.bkap.aispark.api;

import com.bkap.aispark.entity.EmailQueue;
import com.bkap.aispark.entity.EmailStatus;
import com.bkap.aispark.repository.EmailQueueRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/emails")
public class EmailController {

    private final EmailQueueRepository emailQueueRepo;

    public EmailController(EmailQueueRepository emailQueueRepo) {
        this.emailQueueRepo = emailQueueRepo;
    }

    // Gửi lại email theo id
    @PostMapping("/{id}/retry")
    public EmailQueue retryEmail(@PathVariable Long id) {
        Optional<EmailQueue> emailOpt = emailQueueRepo.findById(id);
        if (emailOpt.isEmpty()) {
            throw new RuntimeException("Email not found");
        }

        EmailQueue email = emailOpt.get();
        email.setStatus(EmailStatus.PENDING); // Đưa lại vào hàng chờ
        return emailQueueRepo.save(email);
    }

    // Lấy danh sách tất cả email trong queue
    @GetMapping
    public java.util.List<EmailQueue> getAllEmails() {
        return emailQueueRepo.findAll();
    }
}
