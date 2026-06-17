package com.kamil.aiops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AiopsBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiopsBackendApplication.class, args);
	}

}
