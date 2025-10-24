package com.capstone.BEApp.repository;

import com.capstone.BEApp.entity.ProductFlower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductFlowerRepository extends JpaRepository<ProductFlower, Long> {
    void deleteAllByProductId(Long productId);
}
