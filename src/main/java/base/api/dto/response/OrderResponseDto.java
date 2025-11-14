package base.api.dto.response;

import base.api.entity.DeliveryStatusModel;
import base.api.entity.OrderItemModel;
import base.api.entity.TransactionModel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Setter
@Getter
public class OrderResponseDto {
    private UserDto user;

    private String orderCode;

    private double total;

    private TransactionModel transaction;
    private Long id;

    private List<OrderItemResponseDto> items = new ArrayList<>();

    private List<DeliveryStatusModel> deliveryStatuses = new ArrayList<>();


}
