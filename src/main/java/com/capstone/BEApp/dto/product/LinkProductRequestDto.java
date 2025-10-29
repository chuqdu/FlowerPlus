package com.capstone.BEApp.dto.product;

import lombok.Data;
import java.util.List;

@Data
public class LinkProductRequestDto {
    private Long parentId;
    private List<ChildLinkDto> children;
}

