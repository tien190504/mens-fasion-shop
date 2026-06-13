package com.powerranger.fashion_shop_backend;

import com.powerranger.fashion_shop_backend.config.AppProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class FashionShopBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(FashionShopBackendApplication.class, args);
	}

}
