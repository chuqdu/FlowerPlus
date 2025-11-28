package base.api.dto.request;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckoutDto {
    private Long userId;
    private Long productId;
    private String shippingAddress;
    private String phoneNumber;
    private String returnUrl;
    private String cancelUrl;
    private String note;
    private LocalDateTime requestDeliveryTime;

    private Long orderId;
    private int quantity;
}
