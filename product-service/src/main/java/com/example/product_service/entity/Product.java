package com.example.product_service.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Product {
    private long id;
    private String title;
    private String description;
    private double price;
    private String category;
    private String image;
    private Rating rating;

    @Getter
    @Setter
    public static class Rating {
        private double rate;
        private int count;
    }
}
