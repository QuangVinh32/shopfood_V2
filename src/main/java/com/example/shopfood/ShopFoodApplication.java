

package com.example.shopfood;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShopFoodApplication {
	public static void main(String[] args) {
		SpringApplication.run(ShopFoodApplication.class, args);
	}
}
