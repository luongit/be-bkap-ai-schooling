package com.bkap.aispark.service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bkap.aispark.dto.CreateSchoolResponse;
import com.bkap.aispark.entity.EmailQueue;
import com.bkap.aispark.entity.ObjectType;
import com.bkap.aispark.entity.Schools;
import com.bkap.aispark.entity.User;
import com.bkap.aispark.entity.UserRole;
import com.bkap.aispark.repository.SchoolsRepository;
import com.bkap.aispark.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class SchoolService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final SchoolsRepository schoolRepo;
    private final UserRepository userRepo;
    private final AuditLogService auditLogService;
    private final EmailQueueService emailQueueService;

    public SchoolService(SchoolsRepository schoolRepo,
            UserRepository userRepo,
            AuditLogService auditLogService,
            EmailQueueService emailQueueService) {
        this.schoolRepo = schoolRepo;
        this.userRepo = userRepo;
        this.auditLogService = auditLogService;
        this.emailQueueService = emailQueueService;
    }

    private String generateTempPassword(int length) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Transactional
    public CreateSchoolResponse createSchoolWithAdmin(Schools school) {
        if (userRepo.existsByEmail(school.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        // 1) Lưu school
        Schools savedSchool = schoolRepo.save(school);

        // 2) Tạo user SchoolAdmin với mật khẩu tạm
        String tempPassword = generateTempPassword(10);
        User admin = new User();
        admin.setEmail(savedSchool.getEmail());
        admin.setPassword(passwordEncoder.encode(tempPassword));
        admin.setRole(UserRole.SCHOOL_ADMIN);
        admin.setObjectId(savedSchool.getId()); // Long
        admin.setObjectType(ObjectType.SCHOOL);
        admin.setPhone(savedSchool.getPhone());
        User savedAdmin = userRepo.save(admin);

        // 3) Gán admin_id cho school và lưu
        savedSchool.setAdminId(savedAdmin.getId());
        Schools updatedSchool = schoolRepo.save(savedSchool);

        // 4) Audit log (CREATE_SCHOOL)
        Map<String, Object> details = new HashMap<>();
        details.put("schoolName", updatedSchool.getName());
        details.put("schoolEmail", updatedSchool.getEmail());
        details.put("adminId", savedAdmin.getId());
        auditLogService.logAction(savedAdmin.getId(), "CREATE_SCHOOL", "schools", updatedSchool.getId(), details);

        // 5) Xếp email vào hàng đợi (queue)
        Long emailQueueId = null;
        String emailStatus = "NOT_QUEUED";
        try {
            String subject = "Tài khoản School Admin - " + updatedSchool.getName();
            String content = "Xin chào,\n\nTài khoản School Admin đã được tạo.\nEmail: "
                    + savedAdmin.getEmail()
                    + "\nMật khẩu tạm: " + tempPassword
                    + "\nVui lòng đăng nhập và đổi mật khẩu.\n\nTrân trọng.";
            EmailQueue queued = emailQueueService.queueWelcomeEmail(savedAdmin.getEmail(), subject, content);
            emailQueueId = queued.getId();
            emailStatus = queued.getStatus() != null ? queued.getStatus().name() : "PENDING";
        } catch (Exception ex) {
            // Nếu queue lỗi — ghi audit log lỗi queue và tiếp tục trả về thông tin với
            // trạng thái FAILED
            Map<String, Object> err = new HashMap<>();
            err.put("reason", ex.getMessage());
            auditLogService.logAction(savedAdmin.getId(), "EMAIL_QUEUE_FAILED", "email_queue", null, err);
            emailStatus = "FAILED";
        }

        // 6) Trả DTO cho FE (trong đó chứa mật khẩu tạm để FE hiển thị 1 lần)
        return new CreateSchoolResponse(
                updatedSchool.getId(),
                updatedSchool.getName(),
                savedAdmin.getEmail(),
                tempPassword,
                emailQueueId,
                emailStatus);
    }
}
