package com.bkap.aispark.api;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bkap.aispark.entity.ForbiddenKeyword;
import com.bkap.aispark.service.ForbiddenKeywordService;

@RestController
@RequestMapping("/api/forbidden-keywords")
public class ForbiddenKeywordApi {

    @Autowired
    private ForbiddenKeywordService forbiddenKeywordService;

    // Lấy tất cả keywords
    @GetMapping
    public ResponseEntity<List<ForbiddenKeyword>> getAllForbiddenKeywords() {
        List<ForbiddenKeyword> keywords = forbiddenKeywordService.getAllForbiddenKeywords();
        return ResponseEntity.ok(keywords);
    }

    // Lấy keyword theo id
    @GetMapping("/{id}")
    public ResponseEntity<ForbiddenKeyword> getForbiddenKeywordById(@PathVariable Long id) {
        try {
            ForbiddenKeyword keyword = forbiddenKeywordService.getForbiddenKeywordById(id);
            return ResponseEntity.ok(keyword);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Tạo mới keyword
    @PostMapping
    public ResponseEntity<ForbiddenKeyword> createForbiddenKeyword(@RequestBody ForbiddenKeyword forbiddenKeyword) {
        ForbiddenKeyword createdKeyword = forbiddenKeywordService.createForbiddenKeyword(forbiddenKeyword);
        return ResponseEntity
                .created(URI.create("/api/forbidden-keywords/" + createdKeyword.getId()))
                .body(createdKeyword);
    }

    // Cập nhật keyword theo id
    @PutMapping("/{id}")
    public ResponseEntity<ForbiddenKeyword> updateForbiddenKeyword(@PathVariable Long id,
                                                                   @RequestBody ForbiddenKeyword forbiddenKeyword) {
        try {
            ForbiddenKeyword updatedKeyword = forbiddenKeywordService.updateForbiddenKeyword(id, forbiddenKeyword);
            return ResponseEntity.ok(updatedKeyword);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Xóa keyword theo id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteForbiddenKeyword(@PathVariable Long id) {
        try {
            forbiddenKeywordService.deleteForbiddenKeyword(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
