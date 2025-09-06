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

import com.bkap.aispark.dto.DefaultReplyDTO;
import com.bkap.aispark.entity.DefaultReply;
import com.bkap.aispark.service.DefaultReplyService;

@RestController
@RequestMapping("/api/default-replies")
public class DefaultReplyApi {

    @Autowired
    private DefaultReplyService defaultReplyService;

    @GetMapping
    public ResponseEntity<List<DefaultReply>> getAllDefaultReplies() {
        return ResponseEntity.ok(defaultReplyService.getAllDefaultReplies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDefaultReplyById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(defaultReplyService.getDefaultReplyById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createDefaultReply(@RequestBody DefaultReplyDTO dto) {
        try {
            DefaultReply createdReply = defaultReplyService.createDefaultReply(dto);
            return ResponseEntity
                    .created(URI.create("/api/default-replies/" + createdReply.getId()))
                    .body(createdReply);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDefaultReply(@PathVariable Long id, @RequestBody DefaultReplyDTO dto) {
        try {
            return ResponseEntity.ok(defaultReplyService.updateDefaultReply(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDefaultReply(@PathVariable Long id) {
        try {
            defaultReplyService.deleteDefaultReply(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(new ErrorResponse(e.getMessage()));
        }
    }
}

class ErrorResponse {
    private String message;
    public ErrorResponse(String message) { this.message = message; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}