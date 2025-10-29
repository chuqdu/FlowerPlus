package com.capstone.BEApp.repository;

import com.capstone.BEApp.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByIsDisabledFalse();
    Optional<Product> findByIdAndIsDisabledFalse(Long id);
    @EntityGraph(attributePaths = {
            "images",
    })
    @Query("""
    SELECT DISTINCT parent FROM Product parent
    JOIN parent.components comp
    JOIN comp.childProduct flower
    LEFT JOIN flower.productCategories pc
    LEFT JOIN pc.category c
    WHERE parent.isDisabled = false
      AND parent.type = com.capstone.BEApp.entity.enumFile.ProductType.PRODUCT
      AND flower.type = com.capstone.BEApp.entity.enumFile.ProductType.FLOWER
      AND (
            :categoryId IS NULL
            OR c.id = :categoryId
          )
      AND (
            :keyword IS NULL
            OR LOWER(parent.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(flower.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
""")
    Page<Product> findProductsByFlowerCategoryAndKeyword(
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
            Pageable pageable);

    @EntityGraph(attributePaths = {"images"})
    @Query("""
    SELECT DISTINCT p FROM Product p
    LEFT JOIN p.productCategories pc
    LEFT JOIN pc.category c
    WHERE p.isDisabled = false
      AND (:type IS NULL OR p.type = :type)
      AND (
          :keyword IS NULL
          OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
          OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
          OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
      )
""")
    Page<Product> searchProductsWithType(
            @Param("keyword") String keyword,
            @Param("type") com.capstone.BEApp.entity.enumFile.ProductType type,
            Pageable pageable);

}
