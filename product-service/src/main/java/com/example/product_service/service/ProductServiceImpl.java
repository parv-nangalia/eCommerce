package com.example.product_service.service;

import com.example.product_service.entity.Product;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductServiceImpl implements ProductService {

    private final WebClient webClient;

    public ProductServiceImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Flux<Product> getAllProducts() {
        return webClient.get()
                .uri("/products")
                .retrieve()
                .bodyToFlux(Product.class)
                .onErrorResume(WebClientResponseException.class, e -> {
                    System.err.println("Error fetching all products: " + e.getRawStatusCode() + " - " + e.getMessage());
                    return Flux.error(new RuntimeException("Failed to retrieve products from external API.", e));
                });
    }

    @Override
    public Mono<Product> getProductById(Long id) {
        return webClient.get()
                .uri("/products/{id}", id) // Uses URI variables for path parameters
                .retrieve()
                .bodyToMono(Product.class) // Expects a single Product object in the response body
                .onErrorResume(WebClientResponseException.NotFound.class, e -> {
                    // Handle 404 Not Found specifically
                    System.err.println("Product not found with ID: " + id);
                    return Mono.empty(); // Return empty Mono if product is not found
                })
                .onErrorResume(WebClientResponseException.class, e -> {
                    // Handle other WebClient response exceptions
                    System.err.println("Error fetching product by ID: " + e.getRawStatusCode() + " - " + e.getMessage());
                    return Mono.error(new RuntimeException("Failed to retrieve product from external API.", e));
                });
    }


    public Flux<String> getCategories() {
        return webClient.get()
                .uri("/products/categories")
                .retrieve()
                .bodyToFlux(String.class) // Expects a list of strings
                .onErrorResume(WebClientResponseException.class, e -> {
                    System.err.println("Error fetching categories: " + e.getRawStatusCode() + " - " + e.getMessage());
                    return Flux.error(new RuntimeException("Failed to retrieve categories from external API.", e));
                });
    }


    public Flux<Product> getProductsByCategory(String category) {
        return webClient.get()
                .uri("/products/category/{category}", category)
                .retrieve()
                .bodyToFlux(Product.class)
                .onErrorResume(WebClientResponseException.class, e -> {
                    System.err.println("Error fetching products by category: " + e.getRawStatusCode() + " - " + e.getMessage());
                    return Flux.error(new RuntimeException("Failed to retrieve products by category from external API.", e));
                });
    }

    public Mono<Product> saveProduct(Product product) {
        return webClient.post()
                .uri("/products")
                .bodyValue(product) // Set the request body
                .retrieve()
                .bodyToMono(Product.class)
                .onErrorResume(WebClientResponseException.class, e -> {
                    System.err.println("Error creating product: " + e.getRawStatusCode() + " - " + e.getMessage());
                    return Mono.error(new RuntimeException("Failed to create product via external API.", e));
                });
    }

}
