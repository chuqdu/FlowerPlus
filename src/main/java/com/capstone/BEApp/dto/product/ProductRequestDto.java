package com.capstone.BEApp.dto.product;

import com.capstone.BEApp.entity.enumFile.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequestDto {
    private String name;
    private String description;
    private Integer stock;
    private BigDecimal price;
    private Boolean isPublic;
    private Boolean isCustom;
    private ProductType type;
    private String vector;
    private List<Long> categoryIds;
    private List<String> imageUrls;
    private List<ChildLinkDto> children;
}
