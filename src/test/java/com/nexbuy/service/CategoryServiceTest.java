package com.nexbuy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import com.nexbuy.dto.catalog.*;
import com.nexbuy.entity.*;
import com.nexbuy.exception.ApiException;
import com.nexbuy.patterns.factory.*;
import com.nexbuy.repository.*;
import com.nexbuy.testsupport.TestFixtures;
import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.HttpStatus;

/** Unit tests for CategoryService. */
class CategoryServiceTest {

    @Test
    @DisplayName("list maps every repository category through the factory in repository order")
    void list_maps_every_repository_category_through_the_factory_in_repository_order() throws Exception {
        // Arrange
        // two categories already ordered by the repository
        CategoryRepository categories = mock(CategoryRepository.class);
        CatalogResponseFactory factory = mock(CatalogResponseFactory.class);
        Category books = TestFixtures.category(1L, "Books", "books");
        Category fashion = TestFixtures.category(2L, "Fashion", "fashion");
        CategoryResponse booksResponse = new CategoryResponse(1L, "Books", "books", "Books products");
        CategoryResponse fashionResponse = new CategoryResponse(2L, "Fashion", "fashion", "Fashion products");
        when(categories.findAllByOrderByNameAsc()).thenReturn(List.of(books, fashion));
        when(factory.category(books)).thenReturn(booksResponse);
        when(factory.category(fashion)).thenReturn(fashionResponse);
        CategoryService service = new CategoryService(categories, factory);

        // Act
        // list
        List<CategoryResponse> result = service.list();

        // Assert
        // same order, mapped values
        assertEquals(2, result.size());
        assertSame(booksResponse, result.get(0));
        assertSame(fashionResponse, result.get(1));
        verify(categories).findAllByOrderByNameAsc();
    }

    @Test
    @DisplayName("list returns an empty list when there are no categories")
    void list_returns_an_empty_list_when_there_are_no_categories() throws Exception {
        // Arrange
        // empty repository
        CategoryRepository categories = mock(CategoryRepository.class);
        CatalogResponseFactory factory = mock(CatalogResponseFactory.class);
        when(categories.findAllByOrderByNameAsc()).thenReturn(List.of());
        CategoryService service = new CategoryService(categories, factory);

        // Act
        // list
        List<CategoryResponse> result = service.list();

        // Assert
        // empty and factory untouched
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(factory);
    }

    @Test
    @DisplayName("getBySlug returns the mapped category")
    void getbyslug_returns_the_mapped_category() throws Exception {
        // Arrange
        // category exists
        CategoryRepository categories = mock(CategoryRepository.class);
        CatalogResponseFactory factory = mock(CatalogResponseFactory.class);
        Category books = TestFixtures.category(1L, "Books", "books");
        CategoryResponse expected = new CategoryResponse(1L, "Books", "books", "Books products");
        when(categories.findBySlug("books")).thenReturn(Optional.of(books));
        when(factory.category(books)).thenReturn(expected);
        CategoryService service = new CategoryService(categories, factory);

        // Act
        // fetch
        CategoryResponse response = service.getBySlug("books");

        // Assert
        // mapped response
        assertSame(expected, response);
        assertEquals("books", response.slug());
        verify(categories, times(1)).findBySlug("books");
        verify(factory, times(1)).category(books);
    }

    @Test
    @DisplayName("getBySlug throws 404 for an unknown slug")
    void getbyslug_throws_404_for_an_unknown_slug() throws Exception {
        // Arrange
        // no such category
        CategoryRepository categories = mock(CategoryRepository.class);
        CatalogResponseFactory factory = mock(CatalogResponseFactory.class);
        when(categories.findBySlug("nope")).thenReturn(Optional.empty());
        CategoryService service = new CategoryService(categories, factory);

        // Act
        // fetch missing
        ApiException error = assertThrows(ApiException.class, () -> service.getBySlug("nope"));

        // Assert
        // 404, nothing mapped
        assertEquals("Category not found", error.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        verify(factory, never()).category(any());
    }
}
