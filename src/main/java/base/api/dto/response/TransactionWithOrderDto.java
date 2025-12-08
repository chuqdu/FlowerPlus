package base.api.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionWithOrderDto {
    private Long id;
    private String orderCode;
    private double amount;
    private String status;
    private String checkoutUrl;
    private String paymentLinkId;
    private LocalDateTime createdAt;
    
    // Thông tin order
    private Long orderId;
    private String shippingAddress;
    private String phoneNumber;
    private String note;
    private LocalDateTime requestDeliveryTime;
    
    // Thông tin user
    private Long userId;
    private String userName;
    private String userEmail;
    private String userPhone;
}
