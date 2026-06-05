package com.powerranger.mens_fashion_backend;

import com.powerranger.mens_fashion_backend.config.AppProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class MensFashionBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MensFashionBackendApplication.class, args);
	}

}
