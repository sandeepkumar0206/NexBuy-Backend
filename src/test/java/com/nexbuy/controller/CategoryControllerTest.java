package com.nexbuy.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import com.nexbuy.dto.catalog.*;
import com.nexbuy.service.*;
import java.util.*;
import org.junit.jupiter.api.DisplayName;

/** Day 4 | AC: GET /api/categories and /{slug} | BR: thin delegation to the service */
class CategoryControllerTest {

    // LOW
    @Test
    @DisplayName("list returns the service's categories unchanged")
    void list_returns_the_service_s_categories_unchanged() throws Exception {
        // Arrange
        // service returns one category
        CategoryService service = mock(CategoryService.class);
        List<CategoryResponse> expected = List.of(new CategoryResponse(1L, "Books", "books", "d"));
        when(service.list()).thenReturn(expected);
        CategoryController controller = new CategoryController(service);

        // Act
        // call the endpoint method
        List<CategoryResponse> actual = controller.list();

        // Assert
        // pass-through
        assertSame(expected, actual);
        verify(service).list();
        verifyNoMoreInteractions(service);
    }

    // LOW
    @Test
    @DisplayName("getBySlug forwards the slug to the service")
    void getbyslug_forwards_the_slug_to_the_service() throws Exception {
        // Arrange
        // service returns a category
        CategoryService service = mock(CategoryService.class);
        CategoryResponse expected = new CategoryResponse(1L, "Books", "books", "d");
        when(service.getBySlug("books")).thenReturn(expected);
        CategoryController controller = new CategoryController(service);

        // Act
        // call the endpoint method
        CategoryResponse actual = controller.getBySlug("books");

        // Assert
        // pass-through
        assertSame(expected, actual);
        verify(service).getBySlug("books");
        verifyNoMoreInteractions(service);
    }
}
