package com.bkap.aispark.api;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bkap.aispark.dto.ForbiddenKeywordDTO;
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
public ResponseEntity<ForbiddenKeyword> createForbiddenKeyword(@RequestBody ForbiddenKeywordDTO dto) {
    ForbiddenKeyword createdKeyword = forbiddenKeywordService.createForbiddenKeyword(dto);
    return ResponseEntity
            .created(URI.create("/api/forbidden-keywords/" + createdKeyword.getId()))
            .body(createdKeyword);
}

@PutMapping("/{id}")
public ResponseEntity<ForbiddenKeyword> updateForbiddenKeyword(
        @PathVariable Long id,
        @RequestBody ForbiddenKeywordDTO dto) {
    ForbiddenKeyword updatedKeyword = forbiddenKeywordService.updateForbiddenKeyword(id, dto);
    return ResponseEntity.ok(updatedKeyword);
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
