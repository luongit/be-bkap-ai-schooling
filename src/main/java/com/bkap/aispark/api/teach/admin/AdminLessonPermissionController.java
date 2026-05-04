package com.bkap.aispark.api.teach.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bkap.aispark.service.teach.LessonPermissionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/lessons")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class AdminLessonPermissionController {

    private final LessonPermissionService service;

    @PostMapping("/{lessonId}/permissions")
    public ResponseEntity<?> grantPermission(
            @PathVariable Long lessonId,
            @RequestParam Long teacherId) {

        service.grantViewPermission(lessonId, teacherId);
        return ResponseEntity.ok("Granted permission");
    }

    @DeleteMapping("/{lessonId}/permissions/{teacherId}")
    public ResponseEntity<?> revokePermission(
            @PathVariable Long lessonId,
            @PathVariable Long teacherId) {

        service.revokeViewPermission(lessonId, teacherId);
        return ResponseEntity.ok("Revoked permission");
    }
}
