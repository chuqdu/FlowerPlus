package base.api.service;

import base.api.dto.request.AddTransactionToOrderDto;
import base.api.dto.request.CheckoutDto;
import base.api.dto.request.OrderDto;
import base.api.entity.OrderModel;

import java.util.List;

public interface IOrderService {
    String checkout(CheckoutDto dto) throws Exception;
    List<OrderModel> getAllOrders();
    List<OrderModel> getOrdersByUserId(Long userId);
    String checkoutCustomProduct(CheckoutDto dto) throws Exception;
    String addPaymentToOrder(AddTransactionToOrderDto dto) throws Exception;
}
