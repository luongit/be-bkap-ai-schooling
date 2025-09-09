package com.bkap.aispark.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bkap.aispark.entity.DefaultReply;
import com.bkap.aispark.repository.DefaultReplyRepository;
import com.bkap.aispark.repository.ForbiddenKeywordRepository;
import com.theokanning.openai.service.OpenAiService;

@Service
public class AiChatService {

    @Autowired
    private ForbiddenKeywordRepository forbiddenKeywordRepository;

    @Autowired
    private DefaultReplyRepository defaultReplyRepository;

    @Autowired
    private OpenAiService openAiService;

    // Check keyword cấm
    public boolean containsForbiddenKeyword(String message) {
        if (message == null || message.isBlank()) return false;
        return forbiddenKeywordRepository.findAll().stream()
                .anyMatch(k -> message.toLowerCase().contains(k.getKeyword().toLowerCase()));
    }

    // Lấy reply mặc định theo type
    public String getDefaultForbiddenReply() {
        return defaultReplyRepository.findTopByOrderByIdAsc()
                .map(DefaultReply::getReplyText)
                .orElseThrow(() -> new RuntimeException("Chưa cấu hình default reply trong DB"));
    }


    // Sau này thêm hàm gọi OpenAI vào đây
}


