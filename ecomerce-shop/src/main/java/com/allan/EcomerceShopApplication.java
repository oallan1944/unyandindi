package com.allan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableMethodSecurity
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.allan.repository")
public class EcomerceShopApplication {
	

	public static void main(String[] args) {
		SpringApplication.run(EcomerceShopApplication.class, args);

		// test
	}

}
