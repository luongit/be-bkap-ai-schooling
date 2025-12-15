package com.bkap.aispark.service;

import java.time.LocalDateTime;
import java.util.UUID;

import com.bkap.aispark.entity.*;
import com.bkap.aispark.repository.UserCreditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import com.bkap.aispark.dto.LoginResponse;
import com.bkap.aispark.dto.ParentRegisterRequest;
import com.bkap.aispark.dto.StudentRegisterRequest;
import com.bkap.aispark.repository.ParentRepository;
import com.bkap.aispark.repository.StudentRepository;
import com.bkap.aispark.repository.UserRepository;
import com.bkap.aispark.security.JwtUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;

@Service
public class AuthService {

    @Value("${app.base-url}")
    private String baseUrl;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private  StudentService studentService;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private ParentService parentService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserCreditRepository userCreditRepository;

    public boolean checkPassword(String raw, String encoded) {
        return passwordEncoder.matches(raw, encoded);
    }

    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // ----------------- HELPER -----------------
    private String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }

    private void sendVerificationEmail(String toEmail, String token) {
        String link = baseUrl + "/api/auth/verify?email=" + toEmail + "&token=" + token;

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
                """.formatted(link);

            helper.setText(htmlMsg, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Lỗi gửi email xác minh", e);
        }
    }



    // ----------------- REGISTER STUDENT -----------------
    @Transactional
    public String registerStudent(StudentRegisterRequest req) {

        // Validate username không chứa dấu và khoảng trắng
        if (!req.getUsername().matches("^[a-zA-Z0-9._-]+$")) {
            throw new IllegalArgumentException(
                    "Tên đăng nhập chỉ được chứa chữ cái không dấu, số và ký tự . _ - , không chứa dấu hoặc khoảng trắng");
        }

        if (req.getUsername() == null || req.getEmail() == null || req.getPassword() == null) {
            throw new IllegalArgumentException("Username, email, password là bắt buộc");
        }
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        // Tạo Student entity
        Student student = new Student();
        student.setCode(studentService.generateStudentCode());  // Gọi phương thức từ StudentService
        student.setFullName(req.getFullName());
        student.setBirthdate(req.getBirthdate());
        student.setPhone(req.getPhone());
        student.setEmail(req.getEmail());
        student.setDefaultPassword(req.getPassword());
        student.setUsername(req.getUsername());
        student.setHobbies(req.getHobbies());
        Student savedStudent = studentRepository.save(student);

        // Tạo User inactive + token
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

        // Thêm credit 3000 vào tài khoản
        UserCredit credit = new UserCredit(user, 3000, null);
        userCreditRepository.save(credit);

        return "Đăng ký thành công. Vui lòng kiểm tra email để xác minh tài khoản.";
    }


    // ----------------- REGISTER PARENT -----------------
    @Transactional
    public String registerParent(ParentRegisterRequest req) {

        // Validate username không chứa dấu và khoảng trắng
        if (!req.getUsername().matches("^[a-zA-Z0-9._-]+$")) {
            throw new IllegalArgumentException(
                    "Tên đăng nhập chỉ được chứa chữ cái không dấu, số và ký tự . _ - , không chứa dấu hoặc khoảng trắng");
        }
        if (req.getName() == null || req.getEmail() == null || req.getPassword() == null) {
            throw new IllegalArgumentException("Tên, email, mật khẩu là bắt buộc");
        }
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        Parent parent = new Parent();
        parent.setCode(parentService.generateParentCode());
        parent.setName(req.getName());
        parent.setPhone(req.getPhone());
        parent.setEmail(req.getEmail());
        parent.setAddress(req.getAddress());
        Parent savedParent = parentRepository.save(parent);

        User user = new User();
        user.setEmail(req.getEmail());
        user.setUsername(req.getUsername());
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

        UserCredit credit = new UserCredit(user, 3000, null);
        userCreditRepository.save(credit);

        return "Đăng ký thành công. Vui lòng kiểm tra email để xác minh tài khoản.";
    }


    // ----------------- VERIFY ACCOUNT -----------------
    @Transactional
    public String verifyUserByLink(String email, String token) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        if (user.getIsActive()) {
            return "Tài khoản đã được kích hoạt.";
        }

        if (user.getVerificationCode().equals(token)
                && user.getCodeExpiry().isAfter(LocalDateTime.now())) {

            user.setIsActive(true);
            user.setVerificationCode(null);
            user.setCodeExpiry(null);
            userRepository.save(user);

            //  Sinh cả Access & Refresh Token
            String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getUsername(), user.getRole().name());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail() ,true);

            // Có thể trả JSON thay vì chuỗi text cho frontend dễ xử lý:
            return String.format(
                    "{\"message\": \"Xác minh thành công\", \"accessToken\": \"%s\", \"refreshToken\": \"%s\"}",
                    accessToken, refreshToken
            );

        } else {
            return "Liên kết xác minh không hợp lệ hoặc đã hết hạn.";
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

        //  Sinh Access & Refresh Token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getUsername(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail(),true);

        return new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                user.getObjectType() != null ? user.getObjectType().name() : null,
                user.getObjectId(),
                accessToken,
                refreshToken
        );
    }


}
