package com.stockhub.alert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
// Enable @Scheduled for scheduler
@EnableScheduling
public class AlertServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlertServiceApplication.class, args);
	}

}
