package com.linkt.linkt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.linkt")
@EntityScan("com.linkt.model")
@EnableJpaRepositories("com.linkt.repository")
public class LinktApplication {

	public static void main(String[] args) {
		SpringApplication.run(LinktApplication.class, args);
	}

}
