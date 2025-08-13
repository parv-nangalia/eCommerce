package com.example.product_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class ProductServiceApplication {

	public static void main(String[] args) {

		SpringApplication.run(ProductServiceApplication.class, args);
	}

	@Bean
	public WebClient webClient(WebClient.Builder builder) {
		// Build a WebClient instance with the base URL for FakeStoreAPI.
		// This allows all subsequent requests made with this client to be relative to this base URL.
		return builder.baseUrl("https://fakestoreapi.com").build();
	}
}
