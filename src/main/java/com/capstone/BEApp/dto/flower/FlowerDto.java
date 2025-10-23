package com.capstone.BEApp.dto.flower;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowerDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String quality;
    private String status;
    private String season;
    private LocalDateTime createdDate;
    private List<String> imageUrls;
}
