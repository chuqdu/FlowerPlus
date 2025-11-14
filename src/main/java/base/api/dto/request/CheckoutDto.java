package base.api.dto.request;


import lombok.Data;

@Data
public class CheckoutDto {
    private Long userId;
    private Long productId;
    private String shippingAddress;
    private String phoneNumber;
    private String returnUrl;
    private String cancelUrl;
    private String note;
    private Long orderId;
    private int quantity;
}
