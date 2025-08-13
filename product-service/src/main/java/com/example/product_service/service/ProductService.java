package com.example.product_service.service;

import com.example.product_service.entity.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface ProductService {

    Flux<Product> getAllProducts();
    Mono<Product> getProductById(Long id);
    Flux<String> getCategories();
    Flux<Product> getProductsByCategory(String category);
    Mono<Product> saveProduct(Product product);

}
