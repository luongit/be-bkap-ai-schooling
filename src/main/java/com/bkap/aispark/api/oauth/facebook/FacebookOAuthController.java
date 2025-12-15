package com.bkap.aispark.api.oauth.facebook;

import com.bkap.aispark.entity.*;
import com.bkap.aispark.repository.StudentRepository;
import com.bkap.aispark.repository.UserCreditRepository;
import com.bkap.aispark.repository.UserRepository;
import com.bkap.aispark.security.JwtUtil;
import com.bkap.aispark.service.AuthService;
import com.bkap.aispark.service.StudentService;
import com.bkap.aispark.service.oauth.facebook.FacebookOauthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class FacebookOAuthController {

    private final FacebookOauthService facebookService;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final AuthService authService;
    private final StudentService studentService;
    private final JwtUtil jwtUtil;
    private final UserCreditRepository userCreditRepository;

    @Value("${fe.login.redirect}")
    private String feLoginRedirect;



    @GetMapping(value = "/facebook", produces = "text/html")
    public void redirectToFacebook(HttpServletResponse response) throws IOException {
        String loginUrl = facebookService.buildLoginUrl();

        System.out.println("=== FACEBOOK OAUTH REDIRECT (JS) ===");
        System.out.println("Login URL: " + loginUrl);
        System.out.println("===================================");


        response.setContentType("text/html; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("    <meta charset='UTF-8'>");
        out.println("    <title>Redirecting to Facebook...</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("    <p>Redirecting to Facebook login...</p>");
        out.println("    <script>");
        out.println("        window.location.href = '" + loginUrl + "';");
        out.println("    </script>");
        out.println("</body>");
        out.println("</html>");
        out.flush();
    }


    @GetMapping("/facebook/callback")
    public void facebookCallback(
            @RequestParam("code") String code,
            HttpServletResponse response
    ) throws IOException {


        String accessToken = facebookService.exchangeCodeForToken(code);

        // Lấy thông tin user từ Facebook Graph API
        Map<String, Object> info = facebookService.getUserInfo(accessToken);

        String email = (String) info.get("email");
        String fullName = (String) info.get("name");
        String username = email.split("@")[0];

        // User đã tồn tại trong hệ thống ---
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

        //  check Student ---
        Student student = studentRepository.findByEmail(email).orElse(null);

        if (student == null) {
            student = new Student();
            student.setFullName(fullName);
            student.setUsername(username);
            student.setEmail(email);
            student.setDefaultPassword("FACEBOOK_USER");
            student.setCode(studentService.generateStudentCode());
            student.setIsActive(true);
            student = studentRepository.save(student);
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setUsername(username);
        newUser.setPassword("FACEBOOK_USER");
        newUser.setRole(UserRole.STUDENT);
        newUser.setObjectType(ObjectType.STUDENT);
        newUser.setObjectId(student.getId());
        newUser.setIsActive(true);

        newUser = userRepository.save(newUser);

        UserCredit credit = new UserCredit();
        credit.setUser(newUser);
        credit.setCredit(3000);
        credit.setExpiredDate(null);
        userCreditRepository.save(credit);

        String jwt = jwtUtil.generateAccessToken(
                newUser.getId(),
                newUser.getEmail(),
                newUser.getUsername(),
                newUser.getRole().name()
        );


        response.sendRedirect(feLoginRedirect + "?token=" + jwt);
    }
}
