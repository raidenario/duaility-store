package com.mall.inventory_worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com")
public class InventoryWorkerApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryWorkerApplication.class, args);
	}
}
