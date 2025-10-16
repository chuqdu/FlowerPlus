package com.capstone.BEApp.dto.item;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDto {
    private Long id;
    private String name;
    private Double price;
    private String description;

    private List<Long> imageIds;
    private List<Long> productItemIds;
}
