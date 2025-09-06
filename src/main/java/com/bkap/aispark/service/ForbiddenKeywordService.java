package com.bkap.aispark.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new RuntimeException("Chưa đăng nhập hoặc token không hợp lệ");
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));

        ForbiddenKeyword fk = new ForbiddenKeyword();
        fk.setKeyword(dto.getKeyword());
        fk.setCreatedBy(user);
        return forbiddenKeywordRepository.save(fk);
    }

    // Cập nhật từ khóa cấm
    public ForbiddenKeyword updateForbiddenKeyword(Long id, ForbiddenKeywordDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new RuntimeException("Chưa đăng nhập hoặc token không hợp lệ");
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));

        ForbiddenKeyword existingKeyword = getForbiddenKeywordById(id);
        existingKeyword.setKeyword(dto.getKeyword());
        existingKeyword.setCreatedBy(user);
        return forbiddenKeywordRepository.save(existingKeyword);
    }

    // Xóa từ khóa cấm
    public void deleteForbiddenKeyword(Long id) {
        ForbiddenKeyword existingKeyword = getForbiddenKeywordById(id);
        forbiddenKeywordRepository.delete(existingKeyword);
    }
}