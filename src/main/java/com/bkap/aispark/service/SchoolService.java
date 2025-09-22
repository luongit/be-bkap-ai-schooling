package com.bkap.aispark.service;

import org.springframework.stereotype.Service;

import com.bkap.aispark.entity.ObjectType;
import com.bkap.aispark.entity.Schools;
import com.bkap.aispark.entity.User;
import com.bkap.aispark.entity.UserRole;
import com.bkap.aispark.repository.SchoolsRepository;
import com.bkap.aispark.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.transaction.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class SchoolService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final SchoolsRepository schoolRepo;
    private final UserRepository userRepo;
    private final AuditLogService auditLogService;

    public SchoolService(SchoolsRepository schoolRepo, UserRepository userRepo, AuditLogService auditLogService) {
        this.schoolRepo = schoolRepo;
        this.userRepo = userRepo;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public Schools createSchoolWithAdmin(Schools school) {
        // 1. check email đã tồn tại?
        if (userRepo.existsByEmail(school.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        // 2. Lưu school
        Schools savedSchool = schoolRepo.save(school);

        // 3. Tạo user SchoolAdmin
        User admin = new User();
        admin.setEmail(savedSchool.getEmail());
        admin.setPassword(passwordEncoder.encode("123456")); // TODO: generate random + encode
        admin.setRole(UserRole.SCHOOL_ADMIN);
        admin.setObjectId(savedSchool.getId());
        admin.setObjectType(ObjectType.SCHOOL);
        admin.setPhone(savedSchool.getPhone());
        userRepo.save(admin);

        // 4. Gán admin_id cho school
        savedSchool.setAdminId(admin.getId());
        Schools updatedSchool = schoolRepo.save(savedSchool);

        // 5. Audit log
        Map<String, Object> details = new HashMap<>();
        details.put("schoolName", updatedSchool.getName());
        details.put("schoolEmail", updatedSchool.getEmail());
        details.put("adminId", admin.getId());

        auditLogService.logAction(
                admin.getId(),
                "CREATE_SCHOOL",
                "schools",
                updatedSchool.getId(),
                details);

        return updatedSchool;
    }
}
