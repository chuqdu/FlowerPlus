package com.capstone.BEApp.service.impl;

import com.capstone.BEApp.dto.product.ProductDto;
import com.capstone.BEApp.entity.Product;
import com.capstone.BEApp.entity.ProductFlower;
import com.capstone.BEApp.entity.ProductItems;
import com.capstone.BEApp.repository.ProductRepository;
import com.capstone.BEApp.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Page<ProductDto> searchProducts(
            String keyword,
            String status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {
        Page<Product> products = productRepository.searchProducts(keyword, status, minPrice, maxPrice, pageable);

        return products.map(this::mapToDto);
    }

    private ProductDto mapToDto(Product product) {
        // Lấy tên hoa
        List<String> flowerNames = product.getProductFlowers() != null
                ? product.getProductFlowers().stream()
                .map(ProductFlower::getFlower)
                .filter(f -> f != null)
                .map(f -> f.getName())
                .collect(Collectors.toList())
                : List.of();

        // Lấy tên phụ kiện
        List<String> itemNames = product.getProductItems() != null
                ? product.getProductItems().stream()
                .map(ProductItems::getItems)
                .filter(i -> i != null)
                .map(i -> i.getName())
                .collect(Collectors.toList())
                : List.of();

        // Lấy ảnh chính (ảnh đầu tiên nếu có)
        String mainImageUrl = (product.getImages() != null && !product.getImages().isEmpty())
                ? product.getImages().get(0).getUrl()
                : null;

        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .status(product.getStatus())
                .productPrice(product.getProductPrice())
                .mainImageUrl(mainImageUrl)
                .flowerNames(flowerNames)
                .itemNames(itemNames)
                .build();
    }
}
