package com.nexbuy.patterns.builder;

import com.nexbuy.entity.Category;
import com.nexbuy.entity.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Explicit Builder for Product (alongside Lombok builders on entities). */
public class ProductBuilder {
    private String name;
    private String slug;
    private BigDecimal price;
    private Integer stock = 0;
    private String description;
    private final List<String> images = new ArrayList<>();
    private Category category;

    public ProductBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ProductBuilder slug(String slug) {
        this.slug = slug;
        return this;
    }

    public ProductBuilder price(BigDecimal price) {
        this.price = price;
        return this;
    }

    public ProductBuilder stock(Integer stock) {
        this.stock = stock;
        return this;
    }

    public ProductBuilder description(String description) {
        this.description = description;
        return this;
    }

    public ProductBuilder image(String imageUrl) {
        this.images.add(imageUrl);
        return this;
    }

    public ProductBuilder category(Category category) {
        this.category = category;
        return this;
    }

    public Product build() {
        return Product.builder()
                .name(name)
                .slug(slug)
                .price(price)
                .stock(stock)
                .description(description)
                .images(new ArrayList<>(images))
                .category(category)
                .build();
    }
}
