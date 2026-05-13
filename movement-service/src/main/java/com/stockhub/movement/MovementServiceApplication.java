package com.stockhub.movement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MovementServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MovementServiceApplication.class, args);
	}

}
