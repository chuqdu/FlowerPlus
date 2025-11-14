package base.api.dto.response;

import base.api.entity.OrderItemModel;
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
    private String status = "UNPAID";

    private List<TransactionDto> transactions = new ArrayList<>();

    private List<OrderItemModel> items = new ArrayList<>();


}
