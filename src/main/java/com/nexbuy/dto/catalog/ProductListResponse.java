package com.nexbuy.dto.catalog;

import java.util.List;

public record ProductListResponse(
        List<ProductResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
