package com.example.product_service.controller;

import com.example.product_service.entity.Product;
import com.example.product_service.service.ProductService;
import jdk.jfr.Category;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private ProductService productService;

    public ProductController(ProductService productService) {
        System.out.println("ProductController constructor");
        this.productService = productService;
    }

    @GetMapping
    public Flux<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Product>> getProductById(@PathVariable Long id){
        return productService.getProductById(id)
                .map(product -> ResponseEntity.ok().body(product))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/categories")
    public Flux<String> getAllCategories(){
        return productService.getCategories();
    }

    @GetMapping("/category/{category}")
    public Flux<Product> getProductsByCategory(@PathVariable String category){
        return productService.getProductsByCategory(category);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Product> saveProduct(@RequestBody Product product){
        return productService.saveProduct(product);
    }

}
