package com.megatronix.paridhi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableAsync
@EnableWebMvc
@EnableCaching
@RestController
@EnableScheduling
@SpringBootApplication
@EnableTransactionManagement
public class ParidhiPortal2025Application {
	public static void main(String[] args) {
		SpringApplication.run(ParidhiPortal2025Application.class, args);
		log.info("Portal successfully running on http://localhost:8080");
	}

	@GetMapping
	public String home() {
		return "Hello";
	}
}
