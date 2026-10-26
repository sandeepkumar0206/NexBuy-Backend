package com.nexbuy.dto.catalog;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        String slug,
        BigDecimal price,
        BigDecimal discountedPrice,
        Integer stock,
        boolean inStock,
        String description,
        List<String> images,
        CategoryResponse category
) {
}
