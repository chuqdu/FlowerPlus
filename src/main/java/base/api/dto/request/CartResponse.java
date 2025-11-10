package base.api.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


@Data
public class CartResponse {
    private Long id;
    private Long userId;
    private double totalPrice;
    private List<CartItemResponse> items;
}