package com.capstone.BEApp.dto.item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateItemDto {
    private Long id;
    private String name;
    private Double price;
    private String description;
    private List<String> imageIds;
}