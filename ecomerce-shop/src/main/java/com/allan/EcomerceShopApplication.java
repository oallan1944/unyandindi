package com.allan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
//import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
// @EntityScan(basePackages = "com.allan.model")
public class EcomerceShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcomerceShopApplication.class, args);

		// test
	}

}
