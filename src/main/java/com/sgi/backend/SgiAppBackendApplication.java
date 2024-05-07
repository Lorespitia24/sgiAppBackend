package com.sgi.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SgiAppBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SgiAppBackendApplication.class, args);
	}

}
