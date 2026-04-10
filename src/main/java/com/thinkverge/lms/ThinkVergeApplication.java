package com.thinkverge.lms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@EnableScheduling
@SpringBootApplication
public class ThinkVergeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThinkVergeApplication.class, args);
	}

}
