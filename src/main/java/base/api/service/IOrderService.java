package base.api.service;

import base.api.dto.request.OrderDto;
import base.api.entity.OrderModel;
import vn.payos.type.CheckoutResponseData;

import java.util.List;

public interface IOrderService {
    String checkout(Long userId, String returnUrl, String cancelUrl) throws Exception;
    List<OrderModel> getAllOrders();
    List<OrderModel> getOrdersByUserId(Long userId);
}
