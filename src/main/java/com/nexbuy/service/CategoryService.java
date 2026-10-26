package com.nexbuy.service;

import com.nexbuy.dto.catalog.CategoryResponse;
import com.nexbuy.exception.ApiException;
import com.nexbuy.patterns.factory.CatalogResponseFactory;
import com.nexbuy.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CatalogResponseFactory responseFactory;

    public List<CategoryResponse> list() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(responseFactory::category)
                .toList();
    }

    public CategoryResponse getBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .map(responseFactory::category)
                .orElseThrow(() -> new ApiException("Category not found", HttpStatus.NOT_FOUND));
    }
}
