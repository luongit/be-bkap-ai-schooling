
package com.bkap.aispark.service;

import com.bkap.aispark.entity.DefaultReply;
import com.bkap.aispark.repository.DefaultReplyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
@Service
public class DefaultReplyService {

    @Autowired
    private DefaultReplyRepository defaultReplyRepository;

    public List<DefaultReply> getAllDefaultReplies() {
        return defaultReplyRepository.findAll();
    }

    public DefaultReply getDefaultReplyById(Long id) {
        Optional<DefaultReply> optional = defaultReplyRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        } else {
            throw new RuntimeException("Không tìm thấy câu trả lời mặc định với id: " + id);
        }
    }

    public DefaultReply createDefaultReply(DefaultReply defaultReply) {
        return defaultReplyRepository.save(defaultReply);
    }

    public DefaultReply updateDefaultReply(Long id, DefaultReply defaultReply) {
        DefaultReply existingReply = getDefaultReplyById(id);
        existingReply.setReplyText(defaultReply.getReplyText());
        return defaultReplyRepository.save(existingReply);
    }

    public void deleteDefaultReply(Long id) {
        DefaultReply existingReply = getDefaultReplyById(id);
        defaultReplyRepository.delete(existingReply);
    }
}
