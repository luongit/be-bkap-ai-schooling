package com.bkap.aispark.api.oauth.google;
import com.bkap.aispark.entity.*;
import com.bkap.aispark.repository.StudentRepository;
import com.bkap.aispark.repository.UserCreditRepository;
import com.bkap.aispark.repository.UserRepository;
import com.bkap.aispark.security.JwtUtil;
import com.bkap.aispark.service.AuthService;
import com.bkap.aispark.service.oauth.google.GoogleOauthService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class GoogleOAuthController {
    @Autowired
    private GoogleOauthService googleOAuthService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserCreditRepository userCreditRepository;

    @Value("${fe.login.redirect}")
    private String feLoginRedirect;

    @GetMapping("/google")
    public void redirectToGoogle(HttpServletResponse response) throws IOException {
        response.sendRedirect(googleOAuthService.buildLoginUrl());
    }

    @GetMapping("/google/callback")
    public void googleCallback(
            @RequestParam("code") String code,
            HttpServletResponse response
    ) throws IOException {

        Map<String, Object> token = googleOAuthService.exchangeCodeForToken(code);
        String accessToken = (String) token.get("access_token");

        Map<String, Object> info = googleOAuthService.getUserInfo(accessToken);

        String email = (String) info.get("email");
        String fullName = (String) info.get("name");

        User user = userRepository.findByEmail(email).orElse(null);

        // Nếu đã có User → login luôn
        if (user != null) {
            String jwt = jwtUtil.generateAccessToken(
                    user.getId(), user.getEmail(), user.getUsername(), user.getRole().name()
            );
            response.sendRedirect(feLoginRedirect + "?token=" + jwt);
            return;
        }


        Student student = studentRepository.findByEmail(email).orElse(null);

        String username = email.split("@")[0].replaceAll("[^a-zA-Z0-9._-]", "");

        // Nếu Student chưa có → tạo mới
        if (student == null) {
            student = new Student();
            student.setFullName(fullName);
            student.setUsername(username);
            student.setEmail(email);
            student.setDefaultPassword("GOOGLE_USER");
            student.setCode(authService.generateStudentCodeByYear());
            student.setIsActive(true);

            student = studentRepository.save(student);
        }

        //  Luôn tạo user mapping
        user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword("GOOGLE_USER");
        user.setRole(UserRole.STUDENT);
        user.setObjectType(ObjectType.STUDENT);
        user.setObjectId(student.getId());
        user.setIsActive(true);


        userRepository.save(user);

        UserCredit credit = new UserCredit();
        credit.setUser(user);
        credit.setCredit(3000);
        credit.setExpiredDate(null);
        userCreditRepository.save(credit);

        // ------ JWT ------
        String jwt = jwtUtil.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole().name()
        );

        // Redirect FE
        response.sendRedirect(feLoginRedirect + "?token=" + jwt);
    }

}
