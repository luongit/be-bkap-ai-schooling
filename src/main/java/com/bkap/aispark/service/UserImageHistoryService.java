package com.bkap.aispark.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.bkap.aispark.entity.UserImageHistory;
import com.bkap.aispark.repository.UserImageHistoryRepository;

@Service
public class UserImageHistoryService {

    private final UserImageHistoryRepository imageRepo;

    public UserImageHistoryService(UserImageHistoryRepository imageRepo) {
        this.imageRepo = imageRepo;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(Long userId, String prompt, String style, String size,
            String status, String errorMessage) {
        try {
            UserImageHistory history = new UserImageHistory();
            history.setUserId(userId);
            history.setPrompt(prompt);
            history.setStyle(style);
            history.setSize(size);
            history.setStatus(status);
            history.setErrorMessage(errorMessage);
            imageRepo.save(history);
        } catch (Exception e) {
            System.err.println("❌ Failed to save image history: " + e.getMessage());
        }
    }

}
