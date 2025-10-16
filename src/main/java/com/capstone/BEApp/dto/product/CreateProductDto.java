package com.capstone.BEApp.dto.product;
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
public class CreateProductDto {
    private String name;
    private String description;
    private String status;
    private BigDecimal productPrice;
    private Long categoryId;

    private List<Long> flowerIds;
    private List<Long> itemIds;
    private List<String> imageUrls;
    private String mainImageUrl;

    private List<String> flowerNames;
    private List<String> itemNames;
}
