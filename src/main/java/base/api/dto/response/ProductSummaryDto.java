package base.api.dto.response;
import base.api.entity.ProductCompositionModel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ProductSummaryDto {
    private Long id;
    private String name;
    private String images;
    private double price;
    private List<ProductCompositionDto> compositions = new ArrayList<>();
}