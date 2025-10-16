package com.capstone.BEApp.dto.flower;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFlowerDto {
    private Long id;
    private String name;
    private String quality;
    private String description;
    private BigDecimal price;
    private String season;
    private Long categoryId;
}
