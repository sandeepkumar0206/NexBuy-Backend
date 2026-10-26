package com.nexbuy.testsupport;

import com.nexbuy.dto.catalog.ProductResponse;
import com.nexbuy.entity.Category;
import com.nexbuy.entity.Product;
import com.nexbuy.entity.Role;
import com.nexbuy.entity.User;
import com.nexbuy.patterns.factory.CatalogResponseFactory;
import com.nexbuy.patterns.strategy.NameProductSort;
import com.nexbuy.patterns.strategy.NewestProductSort;
import com.nexbuy.patterns.strategy.PriceAscProductSort;
import com.nexbuy.patterns.strategy.PriceDescProductSort;
import com.nexbuy.patterns.strategy.ProductSortService;
import com.nexbuy.repository.CategoryRepository;
import com.nexbuy.repository.ProductRepository;
import com.nexbuy.service.ProductService;
import org.mockito.ArgumentMatchers;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/** Shared fixtures for unit tests. Plain data builders, no shared state. */
public final class TestFixtures {

    private TestFixtures() {
    }

    public static User user(Long id, String name, String email, Role role) {
        return User.builder().id(id).name(name).email(email).password("hashed").role(role).build();
    }

    public static Category category(Long id, String name, String slug) {
        return Category.builder().id(id).name(name).slug(slug).description(name + " products").build();
    }

    public static Product product(Long id, String name, String slug, String price, Integer stock, Category category) {
        return Product.builder()
                .id(id)
                .name(name)
                .slug(slug)
                .price(new BigDecimal(price))
                .stock(stock)
                .description(name + " description")
                .images(new ArrayList<>())
                .category(category)
                .build();
    }

    public static ProductResponse productResponse(long id) {
        return new ProductResponse(id, "Yoga Mat Pro", "yoga-mat-pro", new BigDecimal("29.99"),
                new BigDecimal("29.99"), 10, true, "Yoga Mat Pro description", List.of(), null);
    }

    public static ProductSortService sortService() {
        return new ProductSortService(List.of(
                new NewestProductSort(), new PriceAscProductSort(), new PriceDescProductSort(), new NameProductSort()));
    }

    /** Builds ProductService with the same @Value defaults as application.properties (12 / 50). */
    public static ProductService productService(
            ProductRepository products,
            CategoryRepository categories,
            ProductSortService sorts,
            CatalogResponseFactory factory
    ) {
        ProductService service = new ProductService(products, categories, sorts, factory);
        ReflectionTestUtils.setField(service, "defaultPageSize", 12);
        ReflectionTestUtils.setField(service, "maxPageSize", 50);
        return service;
    }

    /** Matcher for any Specification argument; call only inside when()/verify() argument lists. */
    public static Specification<Product> anySpec() {
        return ArgumentMatchers.<Specification<Product>>any();
    }

    public static void verifyNoPageQuery(ProductRepository products) {
        verify(products, never()).findAll(anySpec(), any(Pageable.class));
    }
}
