package com.capstone.BEApp.dto.product;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private String status;
    private BigDecimal productPrice;
    private String mainImageUrl;
    private List<String> flowerNames;
    private List<String> itemNames;
}

