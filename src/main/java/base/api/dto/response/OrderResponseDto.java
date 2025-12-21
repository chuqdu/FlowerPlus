package base.api.dto.response;

import base.api.entity.DeliveryStatusModel;
import base.api.entity.OrderItemModel;
import base.api.entity.TransactionModel;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Setter
@Getter
public class OrderResponseDto {
    private String voucherCode;
    private double discountAmount;
    private UserDto user;

    private String orderCode;
    private String shippingAddress;
    private String phoneNumber;
    private String recipientName;
    private String note;

    private double total;
    private LocalDateTime requestDeliveryTime;
    private TransactionModel transaction;
    private Long id;

    private List<OrderItemResponseDto> items = new ArrayList<>();

    private List<DeliveryStatusModel> deliveryStatuses = new ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


}
