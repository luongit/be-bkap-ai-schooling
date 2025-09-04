package com.bkap.aispark.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bkap.aispark.dto.ForbiddenKeywordDTO;
import com.bkap.aispark.entity.ForbiddenKeyword;
import com.bkap.aispark.entity.User;
import com.bkap.aispark.repository.ForbiddenKeywordRepository;
import com.bkap.aispark.repository.UserRepository;

@Service
public class ForbiddenKeywordService {

    @Autowired
    private ForbiddenKeywordRepository forbiddenKeywordRepository;

    @Autowired
    private UserRepository userRepository;

    // Lấy tất cả từ khóa cấm
    public List<ForbiddenKeyword> getAllForbiddenKeywords() {
        return forbiddenKeywordRepository.findAll();
    }

    // Lấy từ khóa cấm theo ID
    public ForbiddenKeyword getForbiddenKeywordById(Long id) {
        return forbiddenKeywordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy từ khóa cấm với id: " + id));
    }

    // Tạo mới từ khóa cấm
    public ForbiddenKeyword createForbiddenKeyword(ForbiddenKeywordDTO dto) {
        ForbiddenKeyword fk = new ForbiddenKeyword();
        fk.setKeyword(dto.getKeyword());

        if (dto.getCreatedById() != null) {
            User user = userRepository.findById(dto.getCreatedById())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user với id: " + dto.getCreatedById()));
            fk.setCreatedBy(user);
        }

        return forbiddenKeywordRepository.save(fk);
    }

    // Cập nhật từ khóa cấm
    public ForbiddenKeyword updateForbiddenKeyword(Long id, ForbiddenKeywordDTO dto) {
        ForbiddenKeyword existingKeyword = getForbiddenKeywordById(id);
        existingKeyword.setKeyword(dto.getKeyword());

        if (dto.getCreatedById() != null) {
            User user = userRepository.findById(dto.getCreatedById())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user với id: " + dto.getCreatedById()));
            existingKeyword.setCreatedBy(user);
        }

        return forbiddenKeywordRepository.save(existingKeyword);
    }

    // Xóa từ khóa cấm
    public void deleteForbiddenKeyword(Long id) {
        ForbiddenKeyword existingKeyword = getForbiddenKeywordById(id);
        forbiddenKeywordRepository.delete(existingKeyword);
    }
}
