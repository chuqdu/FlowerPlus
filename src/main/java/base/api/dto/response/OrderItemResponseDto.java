package base.api.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemResponseDto {

    private Long id;

    private Long productId;
    private String productName;
    private String productImage;

    private double unitPrice;
    private int quantity;
    private double lineTotal;

    private ProductSummaryDto product;
}