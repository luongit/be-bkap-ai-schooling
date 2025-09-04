package com.bkap.aispark.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bkap.aispark.dto.DefaultReplyDTO;
import com.bkap.aispark.entity.DefaultReply;
import com.bkap.aispark.entity.User;
import com.bkap.aispark.repository.DefaultReplyRepository;
import com.bkap.aispark.repository.UserRepository;

@Service
public class DefaultReplyService {

    @Autowired
    private DefaultReplyRepository defaultReplyRepository;

    @Autowired
    private UserRepository userRepository;

    // Lấy tất cả replies
    public List<DefaultReply> getAllDefaultReplies() {
        return defaultReplyRepository.findAll();
    }

    // Lấy reply theo id
    public DefaultReply getDefaultReplyById(Long id) {
        return defaultReplyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy câu trả lời mặc định với id: " + id));
    }

    // Tạo mới reply
    public DefaultReply createDefaultReply(DefaultReplyDTO dto) {
        DefaultReply reply = new DefaultReply();
        reply.setReplyText(dto.getReplyText());

        if (dto.getCreatedById() != null) {
            User user = userRepository.findById(dto.getCreatedById())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user với id: " + dto.getCreatedById()));
            reply.setCreatedBy(user);
        }

        return defaultReplyRepository.save(reply);
    }

    // Cập nhật reply
    public DefaultReply updateDefaultReply(Long id, DefaultReplyDTO dto) {
        DefaultReply existingReply = getDefaultReplyById(id);
        existingReply.setReplyText(dto.getReplyText());

        if (dto.getCreatedById() != null) {
            User user = userRepository.findById(dto.getCreatedById())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user với id: " + dto.getCreatedById()));
            existingReply.setCreatedBy(user);
        }

        return defaultReplyRepository.save(existingReply);
    }

    // Xóa reply
    public void deleteDefaultReply(Long id) {
        DefaultReply existingReply = getDefaultReplyById(id);
        defaultReplyRepository.delete(existingReply);
    }
}
