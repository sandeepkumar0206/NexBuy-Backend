package com.nexbuy.controller;

import com.nexbuy.dto.catalog.CategoryResponse;
import com.nexbuy.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryResponse> list() {
        return categoryService.list();
    }

    @GetMapping("/{slug}")
    public CategoryResponse getBySlug(@PathVariable String slug) {
        return categoryService.getBySlug(slug);
    }
}
