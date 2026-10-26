package com.nexbuy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import com.nexbuy.dto.catalog.*;
import com.nexbuy.entity.*;
import com.nexbuy.exception.ApiException;
import com.nexbuy.patterns.factory.*;
import com.nexbuy.patterns.strategy.*;
import com.nexbuy.repository.*;
import com.nexbuy.testsupport.TestFixtures;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

/** Day 4 | AC: product list, detail, search, by-category APIs with paging/sort/filter | BR: size clamped to max, price range validated, 404 for missing */
class ProductServiceTest {

    // HIGH
    @Test
    @DisplayName("list applies the default page size and newest sort when nothing is requested")
    void list_applies_the_default_page_size_and_newest_sort_when_nothing_is_requested() throws Exception {
        // Arrange
        // empty page from the repository, mock response factory
        ProductRepository products = mock(ProductRepository.class);
        CategoryRepository categories = mock(CategoryRepository.class);
        CatalogResponseFactory factory = mock(CatalogResponseFactory.class);
        Page<Product> page = new PageImpl<>(List.of());
        ProductListResponse expected = new ProductListResponse(List.of(), 0, 12, 0L, 0, true, true);
        when(products.findAll(TestFixtures.anySpec(), any(Pageable.class))).thenReturn(page);
        when(factory.productList(page)).thenReturn(expected);
        ProductService service = TestFixtures.productService(products, categories, TestFixtures.sortService(), factory);

        // Act
        // list with all-null arguments
        ProductListResponse response = service.list(null, null, null, null, null, null);

        // Assert
        // page 0, size 12, id DESC, factory result returned
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(products).findAll(TestFixtures.anySpec(), pageable.capture());
        assertEquals(0, pageable.getValue().getPageNumber());
        assertEquals(12, pageable.getValue().getPageSize());
        assertEquals(Sort.by(Sort.Direction.DESC, "id"), pageable.getValue().getSort());
        assertSame(expected, response);
    }

    // HIGH
    @Test
    @DisplayName("list clamps an oversized page size and a negative page number")
    void list_clamps_an_oversized_page_size_and_a_negative_page_number() throws Exception {
        // Arrange
        // request size 500 on page -3 with sort=name
        ProductRepository products = mock(ProductRepository.class);
        CatalogResponseFactory factory = mock(CatalogResponseFactory.class);
        when(products.findAll(TestFixtures.anySpec(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        ProductService service = TestFixtures.productService(products, mock(CategoryRepository.class), TestFixtures.sortService(), factory);

        // Act
        // list
        service.list(-3, 500, "name", null, null, null);

        // Assert
        // page floored to 0, size capped at 50, name sort
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(products).findAll(TestFixtures.anySpec(), pageable.capture());
        assertEquals(0, pageable.getValue().getPageNumber());
        assertEquals(50, pageable.getValue().getPageSize());
        assertEquals(Sort.by(Sort.Direction.ASC, "name").and(Sort.by(Sort.Direction.ASC, "id")), pageable.getValue().getSort());
        verify(factory).productList(any());
    }

    // HIGH
    @Test
    @DisplayName("list falls back to the default size when size is zero or negative")
    void list_falls_back_to_the_default_size_when_size_is_zero_or_negative() throws Exception {
        // Arrange
        // size 0 requested on page 2 with price_asc
        ProductRepository products = mock(ProductRepository.class);
        CatalogResponseFactory factory = mock(CatalogResponseFactory.class);
        when(products.findAll(TestFixtures.anySpec(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        ProductService service = TestFixtures.productService(products, mock(CategoryRepository.class), TestFixtures.sortService(), factory);

        // Act
        // list
        service.list(2, 0, "price_asc", null, null, null);

        // Assert
        // default size 12 used, page preserved
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(products).findAll(TestFixtures.anySpec(), pageable.capture());
        assertEquals(2, pageable.getValue().getPageNumber());
        assertEquals(12, pageable.getValue().getPageSize());
        assertEquals(Sort.by(Sort.Direction.ASC, "price").and(Sort.by(Sort.Direction.ASC, "id")), pageable.getValue().getSort());
    }

    // HIGH
    @Test
    @DisplayName("list rejects a negative minPrice before touching the repository")
    void list_rejects_a_negative_minprice_before_touching_the_repository() throws Exception {
        // Arrange
        // minPrice -1
        ProductRepository products = mock(ProductRepository.class);
        ProductService service = TestFixtures.productService(products, mock(CategoryRepository.class), TestFixtures.sortService(), mock(CatalogResponseFactory.class));

        // Act
        // list with an invalid bound
        ApiException error = assertThrows(ApiException.class, () -> service.list(null, null, null, null, new BigDecimal("-1"), null));

        // Assert
        // 400 and no query
        assertEquals("minPrice cannot be negative", error.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        TestFixtures.verifyNoPageQuery(products);
    }

    // HIGH
    @Test
    @DisplayName("list rejects a negative maxPrice before touching the repository")
    void list_rejects_a_negative_maxprice_before_touching_the_repository() throws Exception {
        // Arrange
        // maxPrice -0.01
        ProductRepository products = mock(ProductRepository.class);
        ProductService service = TestFixtures.productService(products, mock(CategoryRepository.class), TestFixtures.sortService(), mock(CatalogResponseFactory.class));

        // Act
        // list with an invalid bound
        ApiException error = assertThrows(ApiException.class, () -> service.list(null, null, null, null, null, new BigDecimal("-0.01")));

        // Assert
        // 400 and no query
        assertEquals("maxPrice cannot be negative", error.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        TestFixtures.verifyNoPageQuery(products);
    }

    // HIGH
    @Test
    @DisplayName("list rejects a minPrice greater than maxPrice")
    void list_rejects_a_minprice_greater_than_maxprice() throws Exception {
        // Arrange
        // min 50 > max 10
        ProductRepository products = mock(ProductRepository.class);
        ProductService service = TestFixtures.productService(products, mock(CategoryRepository.class), TestFixtures.sortService(), mock(CatalogResponseFactory.class));

        // Act
        // list with crossed bounds
        ApiException error = assertThrows(ApiException.class, () -> service.list(null, null, null, null, new BigDecimal("50"), new BigDecimal("10")));

        // Assert
        // 400 and no query
        assertEquals("minPrice cannot be greater than maxPrice", error.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        TestFixtures.verifyNoPageQuery(products);
    }

    // HIGH
    @Test
    @DisplayName("list rejects an unsupported sort key and lists the allowed keys")
    void list_rejects_an_unsupported_sort_key_and_lists_the_allowed_keys() throws Exception {
        // Arrange
        // unknown sort key
        ProductRepository products = mock(ProductRepository.class);
        ProductService service = TestFixtures.productService(products, mock(CategoryRepository.class), TestFixtures.sortService(), mock(CatalogResponseFactory.class));

        // Act
        // list with sort=bogus
        ApiException error = assertThrows(ApiException.class, () -> service.list(null, null, "bogus", null, null, null));

        // Assert
        // message names the allowed keys; no query
        assertEquals("Unsupported sort 'bogus'. Use one of: newest, price_asc, price_desc, name", error.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        TestFixtures.verifyNoPageQuery(products);
    }

    // HIGH
    @Test
    @DisplayName("getById returns the factory response for an existing product")
    void getbyid_returns_the_factory_response_for_an_existing_product() throws Exception {
        // Arrange
        // product found, factory maps it
        ProductRepository products = mock(ProductRepository.class);
        CatalogResponseFactory factory = mock(CatalogResponseFactory.class);
        Product product = TestFixtures.product(5L, "Yoga Mat Pro", "yoga-mat-pro", "29.99", 10, TestFixtures.category(1L, "Sports", "sports"));
        ProductResponse expected = TestFixtures.productResponse(5L);
        when(products.findById(5L)).thenReturn(Optional.of(product));
        when(factory.product(product)).thenReturn(expected);
        ProductService service = TestFixtures.productService(products, mock(CategoryRepository.class), TestFixtures.sortService(), factory);

        // Act
        // fetch
        ProductResponse response = service.getById(5L);

        // Assert
        // same response, one lookup, one mapping
        assertSame(expected, response);
        assertEquals(5L, response.id());
        verify(products, times(1)).findById(5L);
        verify(factory, times(1)).product(product);
    }

    // HIGH
    @Test
    @DisplayName("getById throws 404 for an unknown product")
    void getbyid_throws_404_for_an_unknown_product() throws Exception {
        // Arrange
        // nothing stored
        ProductRepository products = mock(ProductRepository.class);
        CatalogResponseFactory factory = mock(CatalogResponseFactory.class);
        when(products.findById(99L)).thenReturn(Optional.empty());
        ProductService service = TestFixtures.productService(products, mock(CategoryRepository.class), TestFixtures.sortService(), factory);

        // Act
        // fetch missing
        ApiException error = assertThrows(ApiException.class, () -> service.getById(99L));

        // Assert
        // 404 and no mapping
        assertEquals("Product not found", error.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        verify(factory, never()).product(any());
    }

    // HIGH
    @Test
    @DisplayName("search queries with the requested page, size and sort")
    void search_queries_with_the_requested_page_size_and_sort() throws Exception {
        // Arrange
        // repository returns an empty page
        ProductRepository products = mock(ProductRepository.class);
        CatalogResponseFactory factory = mock(CatalogResponseFactory.class);
        Page<Product> page = new PageImpl<>(List.of());
        ProductListResponse expected = new ProductListResponse(List.of(), 1, 5, 0L, 0, true, true);
        when(products.findAll(TestFixtures.anySpec(), any(Pageable.class))).thenReturn(page);
        when(factory.productList(page)).thenReturn(expected);
        ProductService service = TestFixtures.productService(products, mock(CategoryRepository.class), TestFixtures.sortService(), factory);

        // Act
        // search with padded text
        ProductListResponse response = service.search("  yoga ", 1, 5, "price_desc");

        // Assert
        // page/size/sort passed through
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(products).findAll(TestFixtures.anySpec(), pageable.capture());
        assertEquals(1, pageable.getValue().getPageNumber());
        assertEquals(5, pageable.getValue().getPageSize());
        assertEquals(Sort.by(Sort.Direction.DESC, "price").and(Sort.by(Sort.Direction.ASC, "id")), pageable.getValue().getSort());
        assertSame(expected, response);
    }

    // HIGH
    @Test
    @DisplayName("search rejects a blank query")
    void search_rejects_a_blank_query() throws Exception {
        // Arrange
        // whitespace only
        ProductRepository products = mock(ProductRepository.class);
        ProductService service = TestFixtures.productService(products, mock(CategoryRepository.class), TestFixtures.sortService(), mock(CatalogResponseFactory.class));

        // Act
        // search
        ApiException error = assertThrows(ApiException.class, () -> service.search("   ", null, null, null));

        // Assert
        // 400 and no query
        assertEquals("Search query is required", error.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        TestFixtures.verifyNoPageQuery(products);
    }

    // HIGH
    @Test
    @DisplayName("search rejects a null query")
    void search_rejects_a_null_query() throws Exception {
        // Arrange
        // missing text
        ProductRepository products = mock(ProductRepository.class);
        ProductService service = TestFixtures.productService(products, mock(CategoryRepository.class), TestFixtures.sortService(), mock(CatalogResponseFactory.class));

        // Act
        // search
        ApiException error = assertThrows(ApiException.class, () -> service.search(null, null, null, null));

        // Assert
        // 400 and no query
        assertEquals("Search query is required", error.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        TestFixtures.verifyNoPageQuery(products);
    }

    // HIGH
    @Test
    @DisplayName("search rejects a query longer than 100 characters")
    void search_rejects_a_query_longer_than_100_characters() throws Exception {
        // Arrange
        // 101 characters
        ProductRepository products = mock(ProductRepository.class);
        ProductService service = TestFixtures.productService(products, mock(CategoryRepository.class), TestFixtures.sortService(), mock(CatalogResponseFactory.class));
        String tooLong = "a".repeat(101);

        // Act
        // search
        ApiException error = assertThrows(ApiException.class, () -> service.search(tooLong, null, null, null));

        // Assert
        // 400 and no query
        assertEquals("Search query must be at most 100 characters", error.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        TestFixtures.verifyNoPageQuery(products);
    }

    // HIGH
    @Test
    @DisplayName("listByCategorySlug looks up the category and pages its products")
    void listbycategoryslug_looks_up_the_category_and_pages_its_products() throws Exception {
        // Arrange
        // category exists, empty page
        ProductRepository products = mock(ProductRepository.class);
        CategoryRepository categories = mock(CategoryRepository.class);
        CatalogResponseFactory factory = mock(CatalogResponseFactory.class);
        Category books = TestFixtures.category(4L, "Books", "books");
        Page<Product> page = new PageImpl<>(List.of());
        ProductListResponse expected = new ProductListResponse(List.of(), 0, 12, 0L, 0, true, true);
        when(categories.findBySlug("books")).thenReturn(Optional.of(books));
        when(products.findAll(TestFixtures.anySpec(), any(Pageable.class))).thenReturn(page);
        when(factory.productList(page)).thenReturn(expected);
        ProductService service = TestFixtures.productService(products, categories, TestFixtures.sortService(), factory);

        // Act
        // list by slug
        ProductListResponse response = service.listByCategorySlug("books", null, null, null);

        // Assert
        // slug resolved once and the page query ran
        assertSame(expected, response);
        verify(categories, times(1)).findBySlug("books");
        verify(products, times(1)).findAll(TestFixtures.anySpec(), any(Pageable.class));
        verify(factory).productList(page);
    }

    // HIGH
    @Test
    @DisplayName("listByCategorySlug throws 404 for an unknown category")
    void listbycategoryslug_throws_404_for_an_unknown_category() throws Exception {
        // Arrange
        // no such slug
        ProductRepository products = mock(ProductRepository.class);
        CategoryRepository categories = mock(CategoryRepository.class);
        when(categories.findBySlug("nope")).thenReturn(Optional.empty());
        ProductService service = TestFixtures.productService(products, categories, TestFixtures.sortService(), mock(CatalogResponseFactory.class));

        // Act
        // list by missing slug
        ApiException error = assertThrows(ApiException.class, () -> service.listByCategorySlug("nope", null, null, null));

        // Assert
        // 404 and no product query
        assertEquals("Category not found", error.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        TestFixtures.verifyNoPageQuery(products);
    }
}
