package com.capstone.BEApp.repository;

import com.capstone.BEApp.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
    SELECT DISTINCT p FROM Product p
    LEFT JOIN p.productFlowers pf
    LEFT JOIN pf.flower f
    LEFT JOIN p.productItems pi
    LEFT JOIN pi.items i
    WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
    AND (:status IS NULL OR p.status = :status)
    AND (:minPrice IS NULL OR p.productPrice >= :minPrice)
    AND (:maxPrice IS NULL OR p.productPrice <= :maxPrice)
    AND (:categoryId IS NULL OR p.category.id = :categoryId)
""")
    Page<Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );


}
