package com.capstone.BEApp.dto.flower;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowerDto {
    private Long id;
    private String name;
    private String quality;
    private String description;
    private BigDecimal price;
    private String status;
    private String season;
    private LocalDateTime createdDate;
    private Long categoryId;
    private String categoryName;
}
