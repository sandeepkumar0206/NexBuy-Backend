package com.nexbuy.controller;

import com.nexbuy.dto.catalog.ProductListResponse;
import com.nexbuy.dto.catalog.ProductResponse;
import com.nexbuy.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ProductListResponse list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        return productService.list(page, size, sort, categoryId, minPrice, maxPrice);
    }

    @GetMapping("/featured")
    public List<ProductResponse> featured() {
        return productService.featured();
    }

    @GetMapping("/search")
    public ProductListResponse search(
            @RequestParam("q") String query,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort
    ) {
        return productService.search(query, page, size, sort);
    }

    @GetMapping("/category/{slug}")
    public ProductListResponse byCategory(
            @PathVariable String slug,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort
    ) {
        return productService.listByCategorySlug(slug, page, size, sort);
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.getById(id);
    }
}
