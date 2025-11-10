package base.api.dto.request;

import lombok.Data;

@Data
public class ProductCompositionDto {
    private Long childProductId;
    private Integer quantity;
}