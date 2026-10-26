package com.nexbuy.dto.catalog;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        String description
) {
}
