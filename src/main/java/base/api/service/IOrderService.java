package base.api.service;

import base.api.dto.request.AddTransactionToOrderDto;
import base.api.dto.request.AiCreateOrderDto;
import base.api.dto.request.CheckoutDto;
import base.api.dto.request.OrderDto;
import base.api.entity.OrderModel;

import java.time.LocalDateTime;
import java.util.List;

public interface IOrderService {
    String checkout(CheckoutDto dto) throws Exception;
    List<OrderModel> getAllOrders();
    List<OrderModel> getOrdersByUserId(Long userId);
    List<OrderModel> getOrdersByUserIdAndVoucherId(Long userId, Long voucherId);
    String checkoutCustomProduct(CheckoutDto dto) throws Exception;
    String addPaymentToOrder(AddTransactionToOrderDto dto) throws Exception;
    void handlePaymentSuccess(String orderCode) throws Exception;
    void cancelOrder(Long orderId, Long userId, String reason) throws Exception;
    List<base.api.dto.response.RefundRequestDto> getAllRefundRequests();
    List<base.api.dto.response.RefundRequestDto> getUserRefundRequests(Long userId);
    void processRefund(Long refundId, Long adminId, base.api.dto.request.ProcessRefundDto dto) throws Exception;
    void updateRequestDeliveryTime(Long orderId, LocalDateTime requestDeliveryTime) throws Exception;
    String createOrderForAi(AiCreateOrderDto dto) throws Exception;
}
