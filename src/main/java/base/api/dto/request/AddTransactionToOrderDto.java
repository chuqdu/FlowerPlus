package base.api.dto.request;

import lombok.Data;

@Data
public class AddTransactionToOrderDto {
    private Long orderId;
    private Long userId;
    private String returnUrl;
    private String cancelUrl;
    private double amount;
}
