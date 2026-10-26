package com.nexbuy.patterns.builder;

import com.nexbuy.entity.Category;

/** Explicit Builder for Category (alongside Lombok builders on entities). */
public class CategoryBuilder {
    private String name;
    private String slug;
    private String description;

    public CategoryBuilder name(String name) {
        this.name = name;
        return this;
    }

    public CategoryBuilder slug(String slug) {
        this.slug = slug;
        return this;
    }

    public CategoryBuilder description(String description) {
        this.description = description;
        return this;
    }

    public Category build() {
        return Category.builder()
                .name(name)
                .slug(slug)
                .description(description)
                .build();
    }
}
