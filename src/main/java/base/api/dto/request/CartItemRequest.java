package base.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartItemRequest {
    @NotNull
    private Long productId;
    @Min(1)
    private Integer quantity;
}
