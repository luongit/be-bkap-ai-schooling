package com.bkap.aispark.api;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bkap.aispark.dto.ForbiddenKeywordDTO;
import com.bkap.aispark.dto.UserDTO;
import com.bkap.aispark.entity.ForbiddenKeyword;
import com.bkap.aispark.entity.User;
import com.bkap.aispark.repository.ForbiddenKeywordRepository;
import com.bkap.aispark.repository.UserRepository;

@RestController
@RequestMapping("/api/forbidden-keywords")
public class ForbiddenKeywordApi {

    @Autowired
    private ForbiddenKeywordRepository forbiddenKeywordRepository;

    @Autowired
    private UserRepository userRepository;

    // Lấy tất cả keywords
    @GetMapping
    public ResponseEntity<List<ForbiddenKeywordDTO>> getAllForbiddenKeywords() {
        List<ForbiddenKeyword> keywords = forbiddenKeywordRepository.findAll();
        List<ForbiddenKeywordDTO> keywordDTOs = keywords.stream()
                .map(keyword -> new ForbiddenKeywordDTO(
                        keyword.getId(),
                        keyword.getKeyword(),
                        keyword.getCreatedBy() != null
                                ? new UserDTO(
                                        keyword.getCreatedBy().getId(),
                                        keyword.getCreatedBy().getUsername(),
                                        keyword.getCreatedBy().getEmail())
                                : null
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(keywordDTOs);
    }

    // Lấy keyword theo id
    @GetMapping("/{id}")
    public ResponseEntity<?> getForbiddenKeywordById(@PathVariable Long id) {
        try {
            ForbiddenKeyword keyword = forbiddenKeywordRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy keyword với id: " + id));
            ForbiddenKeywordDTO dto = new ForbiddenKeywordDTO(
                    keyword.getId(),
                    keyword.getKeyword(),
                    keyword.getCreatedBy() != null
                            ? new UserDTO(
                                    keyword.getCreatedBy().getId(),
                                    keyword.getCreatedBy().getUsername(),
                                    keyword.getCreatedBy().getEmail())
                            : null
            );
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(new ErrorResponse("Không tìm thấy keyword: " + e.getMessage()));
        }
    }

    // Tạo mới keyword
    @PostMapping
    public ResponseEntity<?> createForbiddenKeyword(@RequestBody ForbiddenKeywordDTO dto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
                return ResponseEntity.status(401).body(new ErrorResponse("Chưa đăng nhập hoặc token không hợp lệ"));
            }
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));

            ForbiddenKeyword keyword = new ForbiddenKeyword();
            keyword.setKeyword(dto.getKeyword());
            keyword.setCreatedBy(user);
            ForbiddenKeyword createdKeyword = forbiddenKeywordRepository.save(keyword);

            ForbiddenKeywordDTO createdDTO = new ForbiddenKeywordDTO(
                    createdKeyword.getId(),
                    createdKeyword.getKeyword(),
                    new UserDTO(user.getId(), user.getUsername(), user.getEmail())
            );
            return ResponseEntity
                    .created(URI.create("/api/forbidden-keywords/" + createdKeyword.getId()))
                    .body(createdDTO);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new ErrorResponse("Lỗi khi tạo keyword: " + e.getMessage()));
        }
    }

    // Cập nhật keyword
    @PutMapping("/{id}")
    public ResponseEntity<?> updateForbiddenKeyword(@PathVariable Long id, @RequestBody ForbiddenKeywordDTO dto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
                return ResponseEntity.status(401).body(new ErrorResponse("Chưa đăng nhập hoặc token không hợp lệ"));
            }
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));

            ForbiddenKeyword keyword = forbiddenKeywordRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy keyword với id: " + id));
            keyword.setKeyword(dto.getKeyword());
            keyword.setCreatedBy(user);
            ForbiddenKeyword updatedKeyword = forbiddenKeywordRepository.save(keyword);

            ForbiddenKeywordDTO updatedDTO = new ForbiddenKeywordDTO(
                    updatedKeyword.getId(),
                    updatedKeyword.getKeyword(),
                    new UserDTO(user.getId(), user.getUsername(), user.getEmail())
            );
            return ResponseEntity.ok(updatedDTO);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new ErrorResponse("Lỗi khi cập nhật keyword: " + e.getMessage()));
        }
    }

    // Xóa keyword theo id
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteForbiddenKeyword(@PathVariable Long id) {
        try {
            ForbiddenKeyword keyword = forbiddenKeywordRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy keyword với id: " + id));
            forbiddenKeywordRepository.delete(keyword);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(new ErrorResponse("Không tìm thấy keyword: " + e.getMessage()));
        }
    }
}