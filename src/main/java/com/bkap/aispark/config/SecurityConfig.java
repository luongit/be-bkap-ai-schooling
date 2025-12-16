package com.bkap.aispark.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.bkap.aispark.security.JwtFilter;


@EnableMethodSecurity(prePostEnabled = true)
@Configuration
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Cho phép login API không cần token, còn lại phải check JWT
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // .requestMatchers("/api/auth/**").permitAll() // login/register thoải mái
                        // .anyRequest().authenticated()
                        // còn lại cần token
                        .requestMatchers("/favicon/**","/favicon.ico", "/css/**", "/js/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/journalism/create/**").hasAnyRole("SYSTEM_ADMIN", "ADMIN", "TEACHER")

                        .anyRequest().permitAll()

                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "https://bkapai.vn",
                "http://localhost:5173"
        ));
        config.setAllowedMethods(List.of("GET", "POST","PATCH", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        //  cho phep cookie di qua
        config.setAllowCredentials(true);

        // Cho FE reading Set-Cookie
        config.setExposedHeaders(List.of("Set-Cookie"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}