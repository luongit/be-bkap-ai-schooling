package com.bkap.aispark.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bkap.aispark.dto.LoginResponse;
import com.bkap.aispark.dto.ParentRegisterRequest;
import com.bkap.aispark.dto.StudentRegisterRequest;
import com.bkap.aispark.entity.ObjectType;
import com.bkap.aispark.entity.Parent;
import com.bkap.aispark.entity.Student;
import com.bkap.aispark.entity.User;
import com.bkap.aispark.entity.UserRole;
import com.bkap.aispark.repository.ParentRepository;
import com.bkap.aispark.repository.StudentRepository;
import com.bkap.aispark.repository.UserRepository;
import com.bkap.aispark.security.JwtUtil;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JavaMailSender mailSender;

    // ----------------- HELPER -----------------
    private String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }

    private void sendVerificationEmail(String toEmail, String token) {
        String link = "http://bkapai.vn/api/auth/verify?email=" + toEmail + "&token=" + token;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("BKAP AI - Xác minh tài khoản");

            String htmlMsg = """
                        <div style="font-family: Arial, sans-serif; text-align: center; background-color: #f7f7f7; padding: 30px;">
                            <div style="background-color: #ffffff; padding: 30px; border-radius: 10px; display: inline-block; max-width: 500px;">
                                <h2 style="color: #333333;">Xin chào,</h2>
                                <p style="color: #555555; font-size: 16px;">
                                    Vui lòng nhấn vào nút bên dưới để kích hoạt tài khoản của bạn:
                                </p>
                                <a href="%s"
                                   style="display: inline-block; margin-top: 20px; background-color: #007BFF; color: white;
                                          padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold;">
                                    Xác minh tài khoản
                                </a>
                                <p style="color: #999999; font-size: 13px; margin-top: 20px;">
                                    Liên kết sẽ hết hạn sau 15 phút.
                                </p>
                            </div>
                        </div>
                    """
                    .formatted(link);

            helper.setText(htmlMsg, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Lỗi gửi email xác minh", e);
        }

    }

    // ----------------- SINH MÃ HỌC SINH -----------------
    private String generateStudentCode() {
        String code;
        do {
            code = "HS" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        } while (studentRepository.existsByCode(code));
        return code;
    }

    // ----------------- REGISTER STUDENT -----------------
    @Transactional
    public String registerStudent(StudentRegisterRequest req) {

        // 1️⃣ Kiểm tra null / rỗng
        if (req.getUsername() == null || req.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập là bắt buộc");
        }
        if (req.getEmail() == null || req.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email là bắt buộc");
        }
        if (req.getPassword() == null || req.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu là bắt buộc");
        }

        // 2️⃣ Validate định dạng username (sau khi chắc chắn không null)
        if (!req.getUsername().matches("^[a-zA-Z0-9._-]+$")) {
            throw new IllegalArgumentException(
                    "Tên đăng nhập chỉ được chứa chữ cái không dấu, số và ký tự . _ - , không chứa dấu hoặc khoảng trắng");
        }

        // 3️⃣ Kiểm tra trùng
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        if (userRepository.findByPhone(req.getPhone()).isPresent()) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại");
        }

        // ✅ Tiếp tục logic tạo user
        Student student = new Student();
        student.setCode(generateStudentCode());
        student.setFullName(req.getFullName());
        student.setBirthdate(req.getBirthdate());
        student.setPhone(req.getPhone());
        student.setEmail(req.getEmail());
        student.setDefaultPassword(req.getPassword());
        student.setUsername(req.getUsername());
        student.setHobbies(req.getHobbies());
        Student savedStudent = studentRepository.save(student);

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(UserRole.STUDENT);
        user.setIsActive(false);
        user.setObjectId(savedStudent.getId());
        user.setObjectType(ObjectType.STUDENT);

        String token = generateVerificationToken();
        user.setVerificationCode(token);
        user.setCodeExpiry(LocalDateTime.now().plusMinutes(15));

        userRepository.save(user);
        sendVerificationEmail(user.getEmail(), token);

        return "Đăng ký thành công. Vui lòng kiểm tra email để xác minh tài khoản.";
    }

    // ----------------- REGISTER PARENT -----------------
    @Transactional
    public String registerParent(ParentRegisterRequest req) {

        // 1️⃣ Kiểm tra null / rỗng
        if (req.getUsername() == null || req.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập là bắt buộc");
        }
        if (req.getName() == null || req.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên là bắt buộc");
        }
        if (req.getEmail() == null || req.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email là bắt buộc");
        }
        if (req.getPassword() == null || req.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu là bắt buộc");
        }

        // 2️⃣ Validate định dạng username
        if (!req.getUsername().matches("^[a-zA-Z0-9._-]+$")) {
            throw new IllegalArgumentException(
                    "Tên đăng nhập chỉ được chứa chữ cái không dấu, số và ký tự . _ - , không chứa dấu hoặc khoảng trắng");
        }

        // 3️⃣ Kiểm tra trùng
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        if (userRepository.findByPhone(req.getPhone()).isPresent()) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại");
        }

        // ✅ Tiếp tục logic tạo user
        Parent parent = new Parent();
        parent.setName(req.getName());
        parent.setPhone(req.getPhone());
        parent.setEmail(req.getEmail());
        parent.setAddress(req.getAddress());
        Parent savedParent = parentRepository.save(parent);

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(UserRole.PARENT);
        user.setIsActive(false);
        user.setObjectId(savedParent.getId());
        user.setObjectType(ObjectType.PARENT);

        String token = generateVerificationToken();
        user.setVerificationCode(token);
        user.setCodeExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);
        sendVerificationEmail(user.getEmail(), token);

        return "Đăng ký thành công. Vui lòng kiểm tra email để xác minh tài khoản.";
    }

    // ----------------- VERIFY ACCOUNT -----------------
    @Transactional
    public String verifyUserByLink(String email, String token) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        if (user.getIsActive()) {
            return "Tài khoản đã active";
        }

        if (user.getVerificationCode().equals(token)
                && user.getCodeExpiry().isAfter(LocalDateTime.now())) {

            user.setIsActive(true);
            user.setVerificationCode(null);
            user.setCodeExpiry(null);
            userRepository.save(user);

            String jwtToken = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
            return "Xác minh thành công. Token: " + jwtToken;
        } else {
            return "Link xác nhận không hợp lệ hoặc đã hết hạn";
        }
    }

    // ----------------- LOGIN -----------------
    public LoginResponse login(String identifier, String password) {
        User user = identifier.contains("@")
                ? userRepository.findByEmail(identifier)
                        .orElseThrow(() -> new RuntimeException("Email không tồn tại"))
                : userRepository.findByUsername(identifier)
                        .orElseThrow(() -> new RuntimeException("Username không tồn tại"));

        if (!user.getIsActive()) {
            throw new RuntimeException("Tài khoản chưa được kích hoạt");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Mật khẩu không đúng");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                user.getObjectType() != null ? user.getObjectType().name() : null,
                user.getObjectId(),
                token);
    }
}
