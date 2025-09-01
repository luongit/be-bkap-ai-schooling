package com.bkap.aispark.service;

import com.bkap.aispark.entity.ForbiddenKeyword;
import com.bkap.aispark.repository.ForbiddenKeywordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class ForbiddenKeywordService {

    @Autowired
    private ForbiddenKeywordRepository forbiddenKeywordRepository;

    public List<ForbiddenKeyword> getAllForbiddenKeywords() {
        return forbiddenKeywordRepository.findAll();
    }

    public ForbiddenKeyword getForbiddenKeywordById(Long id) {
        Optional<ForbiddenKeyword> optional = forbiddenKeywordRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        } else {
            throw new RuntimeException("Không tìm thấy từ khóa cấm với id: " + id);
        }
    }

    public ForbiddenKeyword createForbiddenKeyword(ForbiddenKeyword forbiddenKeyword) {
        return forbiddenKeywordRepository.save(forbiddenKeyword);
    }

    public ForbiddenKeyword updateForbiddenKeyword(Long id, ForbiddenKeyword forbiddenKeyword) {
        ForbiddenKeyword existingKeyword = getForbiddenKeywordById(id);
        existingKeyword.setKeyword(forbiddenKeyword.getKeyword());
        return forbiddenKeywordRepository.save(existingKeyword);
    }

    public void deleteForbiddenKeyword(Long id) {
        ForbiddenKeyword existingKeyword = getForbiddenKeywordById(id);
        forbiddenKeywordRepository.delete(existingKeyword);
    }
}
