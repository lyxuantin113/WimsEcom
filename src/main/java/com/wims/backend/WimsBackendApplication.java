package com.wims.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableAsync
//@EnableCaching
public class WimsBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(WimsBackendApplication.class, args);
	}

}
