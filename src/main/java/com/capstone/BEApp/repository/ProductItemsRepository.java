package com.capstone.BEApp.repository;

import com.capstone.BEApp.entity.ProductItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductItemsRepository extends JpaRepository<ProductItems, Long> {
    void deleteAllByProductId(Long productId);
}
