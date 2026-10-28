package com.nexbuy.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import com.nexbuy.entity.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

/** Unit tests for ProductSpecifications. */
class ProductSpecificationsTest {

    @Test
    @DisplayName("inStock requires stock greater than zero")
    void instock_requires_stock_greater_than_zero() throws Exception {
        // Arrange
        // root.get("stock") and a greaterThan predicate
        Root<Product> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path stockPath = mock(Path.class);
        Predicate greater = mock(Predicate.class);
        when(root.get("stock")).thenReturn(stockPath);
        when(cb.greaterThan(stockPath, 0)).thenReturn(greater);

        // Act
        // evaluate
        Predicate predicate = ProductSpecifications.inStock().toPredicate(root, query, cb);

        // Assert
        // the greaterThan predicate is returned
        assertSame(greater, predicate);
        verify(cb).greaterThan(stockPath, 0);
    }

    @Test
    @DisplayName("inCategory returns no predicate when the id is null")
    void incategory_returns_no_predicate_when_the_id_is_null() throws Exception {
        // Arrange
        // mocked criteria objects
        Root<Product> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Specification<Product> spec = ProductSpecifications.inCategory(null);

        // Act
        // evaluate
        Predicate predicate = spec.toPredicate(root, query, cb);

        // Assert
        // skipped filter touches nothing
        assertNull(predicate);
        verifyNoInteractions(root);
        verifyNoInteractions(cb);
    }

    @Test
    @DisplayName("inCategory compares the category id")
    void incategory_compares_the_category_id() throws Exception {
        // Arrange
        // root.get("category").get("id") chain
        Root<Product> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path categoryPath = mock(Path.class);
        Path idPath = mock(Path.class);
        Predicate equal = mock(Predicate.class);
        when(root.get("category")).thenReturn(categoryPath);
        when(categoryPath.get("id")).thenReturn(idPath);
        when(cb.equal(idPath, 5L)).thenReturn(equal);
        Specification<Product> spec = ProductSpecifications.inCategory(5L);

        // Act
        // evaluate
        Predicate predicate = spec.toPredicate(root, query, cb);

        // Assert
        // the equality predicate is returned
        assertSame(equal, predicate);
        verify(root).get("category");
        verify(categoryPath).get("id");
        verify(cb).equal(idPath, 5L);
    }

    @Test
    @DisplayName("priceAtLeast returns no predicate when the bound is null")
    void priceatleast_returns_no_predicate_when_the_bound_is_null() throws Exception {
        // Arrange
        // mocked criteria objects
        Root<Product> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Specification<Product> spec = ProductSpecifications.priceAtLeast(null);

        // Act
        // evaluate
        Predicate predicate = spec.toPredicate(root, query, cb);

        // Assert
        // skipped filter touches nothing
        assertNull(predicate);
        verifyNoInteractions(root);
        verifyNoInteractions(cb);
    }

    @Test
    @DisplayName("priceAtLeast builds a greater-than-or-equal predicate")
    void priceatleast_builds_a_greater_than_or_equal_predicate() throws Exception {
        // Arrange
        // root.get("price")
        Root<Product> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path pricePath = mock(Path.class);
        Predicate ge = mock(Predicate.class);
        BigDecimal min = new BigDecimal("10.00");
        when(root.get("price")).thenReturn(pricePath);
        when(cb.greaterThanOrEqualTo(pricePath, min)).thenReturn(ge);
        Specification<Product> spec = ProductSpecifications.priceAtLeast(min);

        // Act
        // evaluate
        Predicate predicate = spec.toPredicate(root, query, cb);

        // Assert
        // the bound predicate is returned
        assertSame(ge, predicate);
        verify(root).get("price");
        verify(cb).greaterThanOrEqualTo(pricePath, min);
    }

    @Test
    @DisplayName("priceAtMost returns no predicate when the bound is null")
    void priceatmost_returns_no_predicate_when_the_bound_is_null() throws Exception {
        // Arrange
        // mocked criteria objects
        Root<Product> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Specification<Product> spec = ProductSpecifications.priceAtMost(null);

        // Act
        // evaluate
        Predicate predicate = spec.toPredicate(root, query, cb);

        // Assert
        // skipped filter touches nothing
        assertNull(predicate);
        verifyNoInteractions(root);
        verifyNoInteractions(cb);
    }

    @Test
    @DisplayName("priceAtMost builds a less-than-or-equal predicate")
    void priceatmost_builds_a_less_than_or_equal_predicate() throws Exception {
        // Arrange
        // root.get("price")
        Root<Product> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path pricePath = mock(Path.class);
        Predicate le = mock(Predicate.class);
        BigDecimal max = new BigDecimal("99.00");
        when(root.get("price")).thenReturn(pricePath);
        when(cb.lessThanOrEqualTo(pricePath, max)).thenReturn(le);
        Specification<Product> spec = ProductSpecifications.priceAtMost(max);

        // Act
        // evaluate
        Predicate predicate = spec.toPredicate(root, query, cb);

        // Assert
        // the bound predicate is returned
        assertSame(le, predicate);
        verify(root).get("price");
        verify(cb).lessThanOrEqualTo(pricePath, max);
    }

    @Test
    @DisplayName("textMatches returns no predicate for null or blank terms")
    void textmatches_returns_no_predicate_for_null_or_blank_terms() throws Exception {
        // Arrange
        // mocked criteria objects and two empty terms
        Root<Product> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Specification<Product> nullSpec = ProductSpecifications.textMatches(null);
        Specification<Product> blankSpec = ProductSpecifications.textMatches("   ");

        // Act
        // evaluate both
        Predicate fromNull = nullSpec.toPredicate(root, query, cb);
        Predicate fromBlank = blankSpec.toPredicate(root, query, cb);

        // Assert
        // both skipped
        assertNull(fromNull);
        assertNull(fromBlank);
        verifyNoInteractions(cb);
    }

    @Test
    @DisplayName("textMatches lowercases the term, escapes LIKE wildcards and ORs name with description")
    void textmatches_lowercases_the_term_escapes_like_wildcards_and_ors_name_with_description() throws Exception {
        // Arrange
        // padded term with %, _ and a backslash
        Root<Product> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path namePath = mock(Path.class);
        Path descPath = mock(Path.class);
        Expression lowered = mock(Expression.class);
        Predicate like = mock(Predicate.class);
        Predicate or = mock(Predicate.class);
        when(root.get("name")).thenReturn(namePath);
        when(root.get("description")).thenReturn(descPath);
        when(cb.lower(any())).thenReturn(lowered);
        when(cb.like(any(Expression.class), anyString(), anyChar())).thenReturn(like);
        when(cb.or(any(Predicate.class), any(Predicate.class))).thenReturn(or);
        Specification<Product> spec = ProductSpecifications.textMatches("  50%_Off\\  ");

        // Act
        // evaluate
        Predicate predicate = spec.toPredicate(root, query, cb);

        // Assert
        // both columns searched with the same escaped pattern, ORed together
        ArgumentCaptor<String> pattern = ArgumentCaptor.forClass(String.class);
        verify(cb, times(2)).like(any(Expression.class), pattern.capture(), eq('\\'));
        assertEquals(List.of("%50\\%\\_off\\\\%", "%50\\%\\_off\\\\%"), pattern.getAllValues());
        verify(root).get("name");
        verify(root).get("description");
        assertSame(or, predicate);
    }
}
