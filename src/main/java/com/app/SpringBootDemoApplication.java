package com.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBootDemoApplication {

	public static void main(String[] args) {
		
		System.out.println("Welcome to SpringBoot..!");
		SpringApplication.run(SpringBootDemoApplication.class, args);
	}

}
