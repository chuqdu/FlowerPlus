package com.capstone.BEApp.dto.product;

import com.capstone.BEApp.dto.category.CategoryDto;
import com.capstone.BEApp.dto.common.ImageDto;
import com.capstone.BEApp.entity.enumFile.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDto {
    private Long id;
    private String name;
    private String description;
    private Integer stock;
    private BigDecimal price;

    private Boolean isPublic;
    private Boolean isCustom;
    private Boolean isDisabled;

    private ProductType type;
    private String vector;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<CategoryDto> categories;
    private List<ImageDto> images;
    private List<ProductChildDto> children;
}
