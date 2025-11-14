package base.api.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductCompositionDto {
    private Integer quantity;

    private Long childProductId;
    private String childProductName;
    private String childProductImages;
    private double childProductPrice;
}