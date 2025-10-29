package com.capstone.BEApp.dto.product;

import com.capstone.BEApp.dto.category.CategoryDto;
import com.capstone.BEApp.dto.common.ImageDto;
import com.capstone.BEApp.entity.enumFile.ProductType;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductChildDto {
    private Long id;
    private String name;
    private ProductType type;
    private BigDecimal price;
    private Integer quantity;
    private List<CategoryDto> categories;
    private List<ImageDto> images;
}
