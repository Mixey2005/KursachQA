package com.example.kursach;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing; // Import

@SpringBootApplication
@EnableJpaAuditing // Enable JPA Auditing for @CreatedDate/@LastModifiedDate
public class KursachApplication {

	public static void main(String[] args) {
		SpringApplication.run(KursachApplication.class, args);
	}

}