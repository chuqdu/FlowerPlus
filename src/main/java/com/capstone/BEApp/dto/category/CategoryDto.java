package com.capstone.BEApp.dto.category;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CategoryDto {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
}