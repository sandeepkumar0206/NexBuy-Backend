package com.nexbuy.repository;

import com.nexbuy.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/** Optional filters for product queries. A null argument means "do not filter". */
public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> inCategory(Long categoryId) {
        return (root, query, cb) -> categoryId == null
                ? null
                : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> priceAtLeast(BigDecimal min) {
        return (root, query, cb) -> min == null
                ? null
                : cb.greaterThanOrEqualTo(root.<BigDecimal>get("price"), min);
    }

    public static Specification<Product> priceAtMost(BigDecimal max) {
        return (root, query, cb) -> max == null
                ? null
                : cb.lessThanOrEqualTo(root.<BigDecimal>get("price"), max);
    }

    /** Case-insensitive match on product name or description. */
    public static Specification<Product> textMatches(String term) {
        return (root, query, cb) -> {
            if (term == null || term.isBlank()) {
                return null;
            }
            String pattern = "%" + escapeLike(term.trim().toLowerCase()) + "%";
            return cb.or(
                    cb.like(cb.lower(root.<String>get("name")), pattern, '\\'),
                    cb.like(cb.lower(root.<String>get("description")), pattern, '\\')
            );
        };
    }

    private static String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
