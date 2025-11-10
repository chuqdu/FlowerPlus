package base.api.dto.request;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class CartItemUpdateRequest {
    @Min(1)
    private Integer quantity;
}