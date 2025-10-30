package com.bkap.aispark.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.bkap.aispark.security.JwtFilter;

@Configuration
public class SecurityConfig {

	@Autowired
	private JwtFilter jwtFilter;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// ✅ 1. Tắt CSRF (REST API không cần)
				.csrf(csrf -> csrf.disable())

				// ✅ 2. Không dùng session
				.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				// ✅ 3. Cho phép truy cập public, chặn API
				.authorizeHttpRequests(auth -> auth
						// ✅ Cho phép toàn bộ static resources
						.requestMatchers("/", "/index.html", "/login.html", "/register.html", "/favicon.*",
								"/manifest.json", "/app.css/**", "/js/**", "/images/**","/admin/**" , "/static/**")
						.permitAll()

						// ✅ Cho phép API auth (login/register)
						.requestMatchers("/auth/**" ,  "/register").permitAll()

						// 🔒 Còn lại (API khác) phải có token
						.anyRequest().authenticated())

				// ✅ 4. Gắn JWT filter trước filter mặc định
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}
