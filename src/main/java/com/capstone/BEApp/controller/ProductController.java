package com.capstone.BEApp.controller;

import com.capstone.BEApp.dto.common.ResponseDto;
import com.capstone.BEApp.dto.product.CreateProductDto;
import com.capstone.BEApp.dto.product.ProductDto;
import com.capstone.BEApp.service.ProductService;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PermitAll
    @GetMapping("/search")
    public ResponseDto<List<ProductDto>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ProductDto> products = productService.searchProducts(
                keyword, status, minPrice, maxPrice, PageRequest.of(page - 1, size), categoryId
        );

        return ResponseDto.successWithPagination(
                products.getContent(),
                "Lấy Giỏ Hoa Thành Công",
                products
        );
    }


    @PostMapping
    public ResponseDto createProduct(@RequestBody CreateProductDto dto) {
        try {
            ProductDto created = productService.createProduct(dto);
            return ResponseDto.success(created,"Tạo sản phẩm thành công");
        } catch (Exception e) {
            return ResponseDto.fail("Lỗi khi thêm sản phẩm: " + e.getMessage());
        }
    }
}