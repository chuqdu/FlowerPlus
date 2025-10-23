package com.capstone.BEApp.dto.flower;

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
public class CreateFlowerDto {
        private String name;
        private String description;
        private BigDecimal price;
        private String quality;
        private String season;
        private List<String> imageUrls;
}
