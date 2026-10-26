package com.nexbuy.patterns.builder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import com.nexbuy.entity.*;
import org.junit.jupiter.api.DisplayName;

/** Day 4 | AC: builder used by seeder | BR: fluent construction */
class CategoryBuilderTest {

    // LOW
    @Test
    @DisplayName("build maps name, slug and description")
    void build_maps_name_slug_and_description() throws Exception {
        // Arrange
        // configured builder
        CategoryBuilder builder = new CategoryBuilder().name("Books").slug("books").description("Reads");

        // Act
        // build
        Category category = builder.build();

        // Assert
        // fields
        assertEquals("Books", category.getName());
        assertEquals("books", category.getSlug());
        assertEquals("Reads", category.getDescription());
        assertNull(category.getId());
    }
}
