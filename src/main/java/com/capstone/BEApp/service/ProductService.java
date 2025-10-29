package com.capstone.BEApp.service;


import com.capstone.BEApp.dto.product.LinkProductRequestDto;
import com.capstone.BEApp.dto.product.ProductRequestDto;
import com.capstone.BEApp.dto.product.ProductResponseDto;
import com.capstone.BEApp.entity.enumFile.ProductType;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    ProductResponseDto createProduct(ProductRequestDto requestDto);
    ProductResponseDto updateProduct(Long id, ProductRequestDto requestDto);
    ProductResponseDto getProduct(Long id);
    List<ProductResponseDto> getAllActiveProducts();
    Page<ProductResponseDto> getProductsByFlowerCategory(Long categoryId, String keyword, int page, int size);
    Page<ProductResponseDto> searchProductsWithType(String keyword, ProductType type, int page, int size);
    void softDeleteProduct(Long id);
    void hardDeleteProduct(Long id);
    void linkProducts(LinkProductRequestDto request);
}
