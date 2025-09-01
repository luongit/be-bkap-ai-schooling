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

import com.bkap.aispark.entity.DefaultReply;
import com.bkap.aispark.service.DefaultReplyService;

@RestController
@RequestMapping("/api/default-replies")
public class DefaultReplyApi {

    @Autowired
    private DefaultReplyService defaultReplyService;

    // Lấy tất cả replies
    @GetMapping
    public ResponseEntity<List<DefaultReply>> getAllDefaultReplies() {
        List<DefaultReply> replies = defaultReplyService.getAllDefaultReplies();
        return ResponseEntity.ok(replies);
    }

    // Lấy reply theo id
    @GetMapping("/{id}")
    public ResponseEntity<DefaultReply> getDefaultReplyById(@PathVariable Long id) {
        try {
            DefaultReply reply = defaultReplyService.getDefaultReplyById(id);
            return ResponseEntity.ok(reply);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Tạo mới reply
    @PostMapping
    public ResponseEntity<DefaultReply> createDefaultReply(@RequestBody DefaultReply defaultReply) {
        DefaultReply createdReply = defaultReplyService.createDefaultReply(defaultReply);
        return ResponseEntity
                .created(URI.create("/api/default-replies/" + createdReply.getId()))
                .body(createdReply);
    }

    // Cập nhật reply theo id
    @PutMapping("/{id}")
    public ResponseEntity<DefaultReply> updateDefaultReply(@PathVariable Long id,
                                                           @RequestBody DefaultReply defaultReply) {
        try {
            DefaultReply updatedReply = defaultReplyService.updateDefaultReply(id, defaultReply);
            return ResponseEntity.ok(updatedReply);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Xóa reply theo id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDefaultReply(@PathVariable Long id) {
        try {
            defaultReplyService.deleteDefaultReply(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
