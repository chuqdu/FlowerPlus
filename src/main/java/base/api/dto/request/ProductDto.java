package base.api.dto.request;

import base.api.enums.ProductType;
import lombok.Data;

import java.util.List;


@Data
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private double price;
    private Integer stock;
    private ProductType productType;
    private Boolean isActive = true;
    private String images;

    private List<Long> categoryIds;

    private List<ProductCompositionDto> compositions;
}