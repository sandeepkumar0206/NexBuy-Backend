package com.nexbuy.service;

import com.nexbuy.dto.catalog.ProductListResponse;
import com.nexbuy.dto.catalog.ProductResponse;
import com.nexbuy.entity.Category;
import com.nexbuy.entity.Product;
import com.nexbuy.exception.ApiException;
import com.nexbuy.patterns.factory.CatalogResponseFactory;
import com.nexbuy.patterns.strategy.ProductSortService;
import com.nexbuy.repository.CategoryRepository;
import com.nexbuy.repository.ProductRepository;
import com.nexbuy.repository.ProductSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private static final int MAX_SEARCH_LENGTH = 100;
    private static final int FEATURED_COUNT = 8;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductSortService sortService;
    private final CatalogResponseFactory responseFactory;

    @Value("${app.catalog.default-page-size:12}")
    private int defaultPageSize;

    @Value("${app.catalog.max-page-size:50}")
    private int maxPageSize;

    public ProductListResponse list(
            Integer page,
            Integer size,
            String sort,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        if (minPrice != null && minPrice.signum() < 0) {
            throw new ApiException("minPrice cannot be negative");
        }
        if (maxPrice != null && maxPrice.signum() < 0) {
            throw new ApiException("maxPrice cannot be negative");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new ApiException("minPrice cannot be greater than maxPrice");
        }

        Specification<Product> spec = Specification
                .where(ProductSpecifications.inCategory(categoryId))
                .and(ProductSpecifications.priceAtLeast(minPrice))
                .and(ProductSpecifications.priceAtMost(maxPrice));
        return findPage(spec, page, size, sort);
    }

    /** Home page picks: the newest in-stock products. */
    public List<ProductResponse> featured() {
        Pageable pageable = PageRequest.of(0, FEATURED_COUNT, sortService.resolve(null));
        return productRepository.findAll(ProductSpecifications.inStock(), pageable)
                .getContent()
                .stream()
                .map(responseFactory::product)
                .toList();
    }

    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApiException("Product not found", HttpStatus.NOT_FOUND));
        return responseFactory.product(product);
    }

    public ProductResponse getBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ApiException("Product not found", HttpStatus.NOT_FOUND));
        return responseFactory.product(product);
    }

    public ProductListResponse search(String query, Integer page, Integer size, String sort) {
        String term = query == null ? "" : query.trim();
        if (term.isEmpty()) {
            throw new ApiException("Search query is required");
        }
        if (term.length() > MAX_SEARCH_LENGTH) {
            throw new ApiException("Search query must be at most " + MAX_SEARCH_LENGTH + " characters");
        }
        return findPage(ProductSpecifications.textMatches(term), page, size, sort);
    }

    public ProductListResponse listByCategorySlug(String slug, Integer page, Integer size, String sort) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ApiException("Category not found", HttpStatus.NOT_FOUND));
        return findPage(ProductSpecifications.inCategory(category.getId()), page, size, sort);
    }

    private ProductListResponse findPage(Specification<Product> spec, Integer page, Integer size, String sort) {
        Pageable pageable = PageRequest.of(
                page == null ? 0 : Math.max(page, 0),
                resolveSize(size),
                sortService.resolve(sort)
        );
        Page<Product> result = productRepository.findAll(spec, pageable);
        return responseFactory.productList(result);
    }

    private int resolveSize(Integer size) {
        if (size == null || size < 1) {
            return defaultPageSize;
        }
        return Math.min(size, maxPageSize);
    }
}
