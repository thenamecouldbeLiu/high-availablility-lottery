package com.interview.lottory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.interview")
@EnableScheduling
@ConfigurationPropertiesScan
public class LottoryApplication {

	public static void main(String[] args) {
		SpringApplication.run(LottoryApplication.class, args);
	}

}
