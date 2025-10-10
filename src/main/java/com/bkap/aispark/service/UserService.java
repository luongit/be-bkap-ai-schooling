package com.bkap.aispark.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bkap.aispark.entity.ObjectType;
import com.bkap.aispark.entity.User;
import com.bkap.aispark.entity.UserRole;
import com.bkap.aispark.repository.StudentRepository;
import com.bkap.aispark.repository.TeacherRepository;
import com.bkap.aispark.repository.UserCreditRepository;
import com.bkap.aispark.repository.UserRepository;

import jakarta.mail.internet.MimeMessage;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private UserCreditRepository userCreditRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository; // Thêm repository

    @Autowired
    private JavaMailSender mailSender;

    public User createUser(Long actorId, User user) {
        // ✅ Nếu là người dùng tự đăng ký
        if (actorId == null) {
            // Gán mặc định các giá trị an toàn
            user.setIsActive(true); // tự kích hoạt
            if (user.getRole() == null) {
                user.setRole(UserRole.STUDENT);
            }
        } else {
            // ✅ Nếu là admin tạo user
            // (có thể thêm kiểm tra quyền tại đây nếu cần)
            user.setIsActive(true);
        }

        // ✅ Lưu user vào DB
        User saved = userRepository.save(user);

        // ✅ Ghi lại log hành động (chỉ khi có actorId)
        auditLogService.logAction(
                actorId,
                "CREATE_USER",
                "users",
                saved.getId(),
                Map.of("after", saved));

        return saved;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User updateUser(Long actorId, Long id, User updatedUser) {
        return userRepository.findById(id).map(user -> {
            Map<String, Object> before = Map.of(
                    "email", user.getEmail(),
                    "phone", user.getPhone(),
                    "isActive", user.getIsActive());

            user.setEmail(updatedUser.getEmail());
            user.setPhone(updatedUser.getPhone());
            user.setIsActive(updatedUser.getIsActive());

            if (updatedUser.getRole() != UserRole.SYSTEM_ADMIN) {
                user.setObjectType(updatedUser.getObjectType());
                user.setObjectId(updatedUser.getObjectId());
            } else {
                user.setObjectType(ObjectType.SYSTEM);
                user.setObjectId(null);
            }

            User saved = userRepository.save(user);

            Map<String, Object> after = Map.of(
                    "email", saved.getEmail(),
                    "phone", saved.getPhone(),
                    "isActive", saved.getIsActive());

            auditLogService.logAction(actorId, "UPDATE_USER", "users", saved.getId(),
                    Map.of("before", before, "after", after));

            return saved;
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Soft delete = deactivate
    public void deactivateUser(Long actorId, Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setIsActive(false);
            userRepository.save(user);

            auditLogService.logAction(actorId, "DEACTIVATE_USER", "users", id,
                    Map.of("isActive", false));
        });
    }

    // Kích hoạt lại
    public void activateUser(Long actorId, Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setIsActive(true);
            userRepository.save(user);

            auditLogService.logAction(actorId, "ACTIVATE_USER", "users", id,
                    Map.of("isActive", true));
        });
    }

    // Hard delete
    @Transactional
    public boolean deleteUser(Long actorId, Long id, String token) {
        return userRepository.findById(id).map(user -> {
            // Kiểm tra quyền của người thực hiện
            User actor = userRepository.findById(actorId)
                    .orElseThrow(() -> new RuntimeException("Người thực hiện không tồn tại"));
            if (actor.getRole() != UserRole.SYSTEM_ADMIN) {
                throw new RuntimeException("Chỉ quản trị viên hệ thống mới có quyền xóa người dùng");
            }

            // Xóa bản ghi liên quan dựa trên objectType
            if (user.getObjectType() != null && user.getObjectId() != null) {
                switch (user.getObjectType()) {
                    case TEACHER:
                        teacherRepository.findById(user.getObjectId()).ifPresent(teacher -> {
                            teacherRepository.delete(teacher);
                            auditLogService.logAction(
                                    actorId,
                                    "DELETE_TEACHER",
                                    "teachers",
                                    user.getObjectId(),
                                    Map.of("message", "Teacher deleted associated with user"));
                        });
                        break;
                    case STUDENT:
                        studentRepository.findById(user.getObjectId()).ifPresent(student -> {
                            studentRepository.delete(student);
                            auditLogService.logAction(
                                    actorId,
                                    "DELETE_STUDENT",
                                    "students",
                                    user.getObjectId(),
                                    Map.of("message", "Student deleted associated with user"));
                        });
                        break;

                }
            }

            // Xóa UserCredit trước
            userCreditRepository.findByUserId(user.getId())
                    .ifPresent(userCreditRepository::delete);

            // Xóa user
            userRepository.delete(user);

            // Ghi log hành động xóa user
            auditLogService.logAction(
                    actorId,
                    "DELETE_USER",
                    "users",
                    id,
                    Map.of("message", "User deleted"));

            return true;
        }).orElse(false);
    }

    // Gửi email
    public void resendAccountEmail(Long actorId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new RuntimeException("User does not have a valid email");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(user.getEmail());
            helper.setSubject("Thông tin tài khoản BkapAI");

            String loginField;
            if (user.getRole() == UserRole.STUDENT) {
                loginField = "Tên đăng nhập (username): " + user.getUsername();
            } else {
                loginField = "Email đăng nhập: " + user.getEmail();
            }

            String content = "Xin chào " +
                    (user.getUsername() != null ? user.getUsername() : "bạn") +
                    ",\n\nTài khoản của bạn đã được tạo." +
                    "\n" + loginField +
                    "\nVui lòng đổi mật khẩu sau khi đăng nhập.";

            helper.setText(content, false); // false = plain text

            mailSender.send(message);

            auditLogService.logAction(actorId, "RESEND_EMAIL", "users", userId,
                    Map.of("email", user.getEmail(), "role", user.getRole().toString()));
        } catch (Exception e) {
            throw new RuntimeException("Không thể gửi email: " + e.getMessage());
        }
    }

}