package com.nexbuy.repository;

import com.nexbuy.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Optional<Product> findBySlug(String slug);

    /** Loads each page's categories in the same query instead of one query per product. */
    @Override
    @EntityGraph(attributePaths = "category")
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);
}
