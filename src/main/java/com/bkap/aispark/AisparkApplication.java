package com.bkap.aispark;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AisparkApplication {

	public static void main(String[] args) {
		SpringApplication.run(AisparkApplication.class, args);
	}

}
