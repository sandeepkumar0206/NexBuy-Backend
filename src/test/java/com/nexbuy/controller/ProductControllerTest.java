package com.nexbuy.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import com.nexbuy.dto.catalog.*;
import com.nexbuy.service.*;
import com.nexbuy.testsupport.TestFixtures;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.DisplayName;

/** Unit tests for ProductController. */
class ProductControllerTest {

    @Test
    @DisplayName("featured returns the service list")
    void featured_returns_the_service_list() throws Exception {
        // Arrange
        // service returns one product
        ProductService service = mock(ProductService.class);
        List<ProductResponse> expected = List.of(TestFixtures.productResponse(1L));
        when(service.featured()).thenReturn(expected);
        ProductController controller = new ProductController(service);

        // Act
        // call the endpoint method
        List<ProductResponse> actual = controller.featured();

        // Assert
        // pass-through
        assertSame(expected, actual);
        verify(service).featured();
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("list forwards all six query parameters to the service")
    void list_forwards_all_six_query_parameters_to_the_service() throws Exception {
        // Arrange
        // service returns a canned page
        ProductService service = mock(ProductService.class);
        ProductListResponse expected = new ProductListResponse(List.of(), 1, 5, 0L, 0, false, true);
        BigDecimal min = new BigDecimal("1");
        BigDecimal max = new BigDecimal("9");
        when(service.list(1, 5, "name", 3L, min, max)).thenReturn(expected);
        ProductController controller = new ProductController(service);

        // Act
        // call the endpoint method
        ProductListResponse actual = controller.list(1, 5, "name", 3L, min, max);

        // Assert
        // pass-through
        assertSame(expected, actual);
        verify(service).list(1, 5, "name", 3L, min, max);
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("search maps q to the query argument")
    void search_maps_q_to_the_query_argument() throws Exception {
        // Arrange
        // service returns a canned page
        ProductService service = mock(ProductService.class);
        ProductListResponse expected = new ProductListResponse(List.of(), 0, 12, 0L, 0, true, true);
        when(service.search("yoga", null, null, "name")).thenReturn(expected);
        ProductController controller = new ProductController(service);

        // Act
        // call the endpoint method
        ProductListResponse actual = controller.search("yoga", null, null, "name");

        // Assert
        // pass-through
        assertSame(expected, actual);
        verify(service).search("yoga", null, null, "name");
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("byCategory forwards the slug and paging arguments")
    void bycategory_forwards_the_slug_and_paging_arguments() throws Exception {
        // Arrange
        // service returns a canned page
        ProductService service = mock(ProductService.class);
        ProductListResponse expected = new ProductListResponse(List.of(), 2, 4, 0L, 0, false, true);
        when(service.listByCategorySlug("books", 2, 4, "price_asc")).thenReturn(expected);
        ProductController controller = new ProductController(service);

        // Act
        // call the endpoint method
        ProductListResponse actual = controller.byCategory("books", 2, 4, "price_asc");

        // Assert
        // pass-through
        assertSame(expected, actual);
        verify(service).listByCategorySlug("books", 2, 4, "price_asc");
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("getById forwards the id to the service")
    void getbyid_forwards_the_id_to_the_service() throws Exception {
        // Arrange
        // service returns a canned product
        ProductService service = mock(ProductService.class);
        ProductResponse expected = TestFixtures.productResponse(7L);
        when(service.getById(7L)).thenReturn(expected);
        ProductController controller = new ProductController(service);

        // Act
        // call the endpoint method
        ProductResponse actual = controller.getById(7L);

        // Assert
        // pass-through
        assertSame(expected, actual);
        verify(service).getById(7L);
        verifyNoMoreInteractions(service);
    }
}
