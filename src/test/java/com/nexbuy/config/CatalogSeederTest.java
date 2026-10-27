package com.nexbuy.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import com.nexbuy.entity.*;
import com.nexbuy.repository.*;
import com.nexbuy.testsupport.TestFixtures;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;

/** Unit tests for CatalogSeeder. */
class CatalogSeederTest {

    @Test
    @DisplayName("run does nothing when products already exist")
    void run_does_nothing_when_products_already_exist() throws Exception {
        // Arrange
        // non-empty catalog
        CategoryRepository categories = mock(CategoryRepository.class);
        ProductRepository products = mock(ProductRepository.class);
        when(products.count()).thenReturn(3L);
        CatalogSeeder seeder = new CatalogSeeder(categories, products);

        // Act
        // run
        seeder.run();

        // Assert
        // no writes
        verify(products).count();
        verify(products, never()).saveAll(any());
        verifyNoInteractions(categories);
    }

    @Test
    @DisplayName("run seeds five categories and twenty-five products on an empty catalog")
    void run_seeds_five_categories_and_twenty_five_products_on_an_empty_catalog() throws Exception {
        // Arrange
        // empty catalog; category saves echo the entity
        CategoryRepository categories = mock(CategoryRepository.class);
        ProductRepository products = mock(ProductRepository.class);
        when(products.count()).thenReturn(0L);
        when(categories.findBySlug(anyString())).thenReturn(Optional.empty());
        when(categories.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CatalogSeeder seeder = new CatalogSeeder(categories, products);

        // Act
        // run
        seeder.run();

        // Assert
        // counts, and the five slugs
        ArgumentCaptor<Category> savedCategories = ArgumentCaptor.forClass(Category.class);
        verify(categories, times(5)).save(savedCategories.capture());
        ArgumentCaptor<List<Product>> savedProducts = ArgumentCaptor.forClass((Class) List.class);
        verify(products, times(1)).saveAll(savedProducts.capture());
        assertEquals(25, savedProducts.getValue().size());
        assertEquals(Set.of("electronics", "fashion", "home-kitchen", "books", "sports-outdoors"),
                savedCategories.getAllValues().stream().map(Category::getSlug).collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    @DisplayName("run gives every seeded product two images, a category and a unique slug, with one item out of stock")
    void run_gives_every_seeded_product_two_images_a_category_and_a_unique_slug_with_one_item_out_of_stock() throws Exception {
        // Arrange
        // empty catalog
        CategoryRepository categories = mock(CategoryRepository.class);
        ProductRepository products = mock(ProductRepository.class);
        when(products.count()).thenReturn(0L);
        when(categories.findBySlug(anyString())).thenReturn(Optional.empty());
        when(categories.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CatalogSeeder seeder = new CatalogSeeder(categories, products);

        // Act
        // run
        seeder.run();

        // Assert
        // data quality
        ArgumentCaptor<List<Product>> savedProducts = ArgumentCaptor.forClass((Class) List.class);
        verify(products).saveAll(savedProducts.capture());
        List<Product> seeded = savedProducts.getValue();
        assertTrue(seeded.stream().allMatch(p -> p.getImages().size() == 2 && p.getCategory() != null));
        assertEquals(25, seeded.stream().map(Product::getSlug).collect(java.util.stream.Collectors.toSet()).size());
        assertEquals(List.of("Cycling Helmet"), seeded.stream().filter(p -> p.getStock() == 0).map(Product::getName).toList());
    }

    @Test
    @DisplayName("run reuses categories that already exist instead of saving them again")
    void run_reuses_categories_that_already_exist_instead_of_saving_them_again() throws Exception {
        // Arrange
        // every slug already stored
        CategoryRepository categories = mock(CategoryRepository.class);
        ProductRepository products = mock(ProductRepository.class);
        when(products.count()).thenReturn(0L);
        when(categories.findBySlug(anyString())).thenAnswer(invocation -> Optional.of(
                TestFixtures.category(1L, "Existing", invocation.getArgument(0))));
        CatalogSeeder seeder = new CatalogSeeder(categories, products);

        // Act
        // run
        seeder.run();

        // Assert
        // no category saves, products still seeded
        verify(categories, never()).save(any(Category.class));
        verify(categories, times(5)).findBySlug(anyString());
        verify(products, times(1)).saveAll(any());
    }
}
