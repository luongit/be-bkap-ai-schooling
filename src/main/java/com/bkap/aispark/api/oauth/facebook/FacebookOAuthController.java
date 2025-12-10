package com.bkap.aispark.api.oauth.facebook;

import com.bkap.aispark.entity.*;
import com.bkap.aispark.repository.StudentRepository;
import com.bkap.aispark.repository.UserCreditRepository;
import com.bkap.aispark.repository.UserRepository;
import com.bkap.aispark.security.JwtUtil;
import com.bkap.aispark.service.AuthService;
import com.bkap.aispark.service.oauth.facebook.FacebookOauthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class FacebookOAuthController {

    private final FacebookOauthService facebookService;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final UserCreditRepository userCreditRepository;

    @Value("${fe.login.redirect}")
    private String feLoginRedirect;


    //  Redirect sang Facebook Login
    @GetMapping("/facebook")
    public void redirectToFacebook(HttpServletResponse response) throws IOException {
        response.sendRedirect(facebookService.buildLoginUrl());
    }


    //  Facebook gọi callback trả code
    @GetMapping("/facebook/callback")
    public void facebookCallback(
            @RequestParam("code") String code,
            HttpServletResponse response
    ) throws IOException {

        // Đổi code → access token
        String accessToken = facebookService.exchangeCodeForToken(code);

        // Lấy thông tin user từ Facebook Graph API
        Map<String, Object> info = facebookService.getUserInfo(accessToken);

        String email = (String) info.get("email");
        String fullName = (String) info.get("name");
        String username = email.split("@")[0];

        // --- CASE 1: User đã tồn tại trong hệ thống ---
        User existingUser = userRepository.findByEmail(email).orElse(null);

        if (existingUser != null) {
            String jwt = jwtUtil.generateAccessToken(
                    existingUser.getId(),
                    existingUser.getEmail(),
                    existingUser.getUsername(),
                    existingUser.getRole().name()
            );
            response.sendRedirect(feLoginRedirect + "?token=" + jwt);
            return;
        }

        // --- User chưa tồn tại → check Student ---
        Student student = studentRepository.findByEmail(email).orElse(null);

        if (student == null) {
            student = new Student();
            student.setFullName(fullName);
            student.setUsername(username);
            student.setEmail(email);
            student.setDefaultPassword("FACEBOOK_USER");
            student.setCode(authService.generateStudentCodeByYear());
            student.setIsActive(true);
            student = studentRepository.save(student);
        }

        // ---  Tạo mới User mapping ---
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setUsername(username);
        newUser.setPassword("FACEBOOK_USER");
        newUser.setRole(UserRole.STUDENT);
        newUser.setObjectType(ObjectType.STUDENT);
        newUser.setObjectId(student.getId());
        newUser.setIsActive(true);

        newUser = userRepository.save(newUser);

        // --- CASE 4: Cấp credit mặc định ---
        UserCredit credit = new UserCredit();
        credit.setUser(newUser);
        credit.setCredit(3000);
        credit.setExpiredDate(null);
        userCreditRepository.save(credit);

        // --- CASE 5: Sinh JWT ---
        String jwt = jwtUtil.generateAccessToken(
                newUser.getId(),
                newUser.getEmail(),
                newUser.getUsername(),
                newUser.getRole().name()
        );

        // --- CASE 6: Redirect về FE ---
        response.sendRedirect(feLoginRedirect + "?token=" + jwt);
    }
}