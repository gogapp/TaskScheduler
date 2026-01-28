package com.meraki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@ComponentScan(basePackages = {"com.meraki"})
@SpringBootApplication
public class WorkerNodeApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkerNodeApplication.class, args);
	}

}
