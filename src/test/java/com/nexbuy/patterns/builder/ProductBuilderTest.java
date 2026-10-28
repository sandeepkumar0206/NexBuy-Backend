package com.nexbuy.patterns.builder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import com.nexbuy.entity.*;
import com.nexbuy.testsupport.TestFixtures;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;

/** Unit tests for ProductBuilder. */
class ProductBuilderTest {

    @Test
    @DisplayName("build maps every configured value to the product")
    void build_maps_every_configured_value_to_the_product() throws Exception {
        // Arrange
        // fully configured builder
        Category category = TestFixtures.category(1L, "Books", "books");
        ProductBuilder builder = new ProductBuilder()
                .name("Java").slug("java").price(new BigDecimal("12.50")).stock(4)
                .description("About Java").image("a.jpg").image("b.jpg").category(category);

        // Act
        // build
        Product product = builder.build();

        // Assert
        // every field
        assertEquals("Java", product.getName());
        assertEquals("java", product.getSlug());
        assertEquals(new BigDecimal("12.50"), product.getPrice());
        assertEquals(4, product.getStock());
        assertEquals("About Java", product.getDescription());
        assertEquals(java.util.List.of("a.jpg", "b.jpg"), product.getImages());
        assertSame(category, product.getCategory());
    }

    @Test
    @DisplayName("build defaults stock to zero with an empty image list")
    void build_defaults_stock_to_zero_with_an_empty_image_list() throws Exception {
        // Arrange
        // builder with only a name
        ProductBuilder builder = new ProductBuilder().name("Bare");

        // Act
        // build
        Product product = builder.build();

        // Assert
        // defaults
        assertEquals(0, product.getStock());
        assertTrue(product.getImages().isEmpty());
        assertNull(product.getCategory());
    }

    @Test
    @DisplayName("build snapshots images so later builder changes do not leak")
    void build_snapshots_images_so_later_builder_changes_do_not_leak() throws Exception {
        // Arrange
        // builder with one image, built once
        ProductBuilder builder = new ProductBuilder().name("Snap").image("a.jpg");
        Product first = builder.build();

        // Act
        // add another image and build again
        Product second = builder.image("b.jpg").build();

        // Assert
        // first product unaffected
        assertEquals(1, first.getImages().size());
        assertEquals(2, second.getImages().size());
        assertNotSame(first.getImages(), second.getImages());
    }
}
