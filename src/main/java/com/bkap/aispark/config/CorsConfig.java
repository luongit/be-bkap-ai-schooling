package com.bkap.aispark.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOriginPatterns("*")
				// .allowedOrigins("*") // hoặc "*" nếu phát triển nội bộ
				.allowedMethods("*")
				.allowedHeaders("*")
				.allowCredentials(true);
	}
}
