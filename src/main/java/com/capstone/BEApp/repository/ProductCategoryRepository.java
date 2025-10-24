package com.capstone.BEApp.repository;

import com.capstone.BEApp.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    void deleteAllByProductId(Long productId);
}

