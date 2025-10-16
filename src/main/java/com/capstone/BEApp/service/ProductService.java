package com.capstone.BEApp.service;

import com.capstone.BEApp.dto.product.ProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ProductService {

    Page<ProductDto> searchProducts(
            String keyword,
            String status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    );

}
