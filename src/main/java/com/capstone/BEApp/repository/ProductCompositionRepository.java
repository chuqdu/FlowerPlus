package com.capstone.BEApp.repository;

import com.capstone.BEApp.entity.Product;
import com.capstone.BEApp.entity.ProductComposition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCompositionRepository extends JpaRepository<ProductComposition, Long> {
    void deleteAllByParentProduct(Product parentProduct);
}
