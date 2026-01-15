package base.api.controller;

import base.api.base.BaseAPIController;
import base.api.dto.request.*;
import base.api.dto.response.OrderResponseDto;
import base.api.dto.response.TFUResponse;
import base.api.entity.OrderModel;
import base.api.service.IDeliveryStatusService;
import base.api.service.IOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.invoices.InvoicesInfo;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController extends BaseAPIController {

    @Autowired
    IOrderService orderService;

    @Autowired
    PayOS payOS;

    @Autowired
    IDeliveryStatusService deliveryStatusService;

    @Autowired
    ModelMapper mapper;

    @PostMapping("checkout")
    public ResponseEntity<TFUResponse<Map<String, Object>>> checkout(@RequestBody CheckoutDto dto) throws Exception {
        Long userId = getCurrentUserId();
        String returnUrl = "https://flowerplus.site/payment/success";
        String cancelUrl = "https://flowerplus.site/payment/failure";
        dto.setUserId(userId);
        dto.setReturnUrl(returnUrl);
        dto.setCancelUrl(cancelUrl);
        String result = orderService.checkout(dto);
        Map<String, Object> data = new HashMap<>();
        data.put("checkoutUrl", result);
        return success(data);
    }

    @PostMapping("checkout-product")
    public ResponseEntity<TFUResponse<Map<String, Object>>> checkoutProduct(@RequestBody CheckoutDto dto) throws Exception {
        Long userId = getCurrentUserId();
        String returnUrl = "https://flowerplus.site/payment/success";
        String cancelUrl = "https://flowerplus.site/payment/failure";
        dto.setUserId(userId);
        dto.setReturnUrl(returnUrl);
        dto.setCancelUrl(cancelUrl);
        String result = orderService.checkoutCustomProduct(dto);
        Map<String, Object> data = new HashMap<>();
        data.put("checkoutUrl", result);
        return success(data);
    }

    @PostMapping("add-transaction-to-order")
    public ResponseEntity<TFUResponse<String>> addTransactionToOrder(
            @RequestBody AddTransactionToOrderDto dto
    ) throws Exception {
        Long userId = getCurrentUserId();
        String returnUrl = "https://flowerplus.site/payment/success";
        String cancelUrl = "https://flowerplus.site/payment/failure";
        dto.setReturnUrl(returnUrl);
        dto.setCancelUrl(cancelUrl);
        dto.setUserId(userId);
        String checkoutUrl = orderService.addPaymentToOrder(dto);
        return success(checkoutUrl);
    }

    @GetMapping("get-list-orders")
    public ResponseEntity<TFUResponse<List<OrderResponseDto>>> getListOrders() throws Exception {
    List<OrderModel> orderModels = orderService.getAllOrders();
    List<OrderResponseDto> orders = orderModels.stream()
            .map(order -> mapper.map(order, OrderResponseDto.class))
            .toList();

        return success(orders);
    }


    @GetMapping("get-list-orders-by-user")
    public ResponseEntity<TFUResponse<List<OrderResponseDto>>> getListOrderByUserId() throws Exception {
        Long userId = getCurrentUserId();
        List<OrderModel> orderModels = orderService.getOrdersByUserId(userId);
        
        // Lọc các orders có payment chưa thành công và lấy 5 cái mới nhất
        List<OrderModel> pendingOrders = orderModels.stream()
                .filter(order -> order.getTransaction() != null 
                        && order.getTransaction().getStatus() != null 
                        && !"SUCCESS".equals(order.getTransaction().getStatus()))
                .limit(5)
                .toList();
        
        // Kiểm tra và cập nhật payment status cho các orders chưa thành công
        for (OrderModel order : pendingOrders) {
            try {
                if (order.getTransaction() != null && order.getTransaction().getOrderCode() != null) {
                    Long orderCode = Long.parseLong(order.getTransaction().getOrderCode());
                    PaymentLink paymentInfo = payOS.paymentRequests().get(orderCode);
                    if ("PAID".equals(paymentInfo.getStatus().toString())) {
                        orderService.handlePaymentSuccess(order.getTransaction().getOrderCode());
                    }
                }
            } catch (Exception e) {
                // Log error nhưng không throw để không ảnh hưởng đến việc trả về danh sách orders
                System.err.println("Error checking payment status for order: " + order.getOrderCode() + " - " + e.getMessage());
            }
        }
        
        // Lấy lại danh sách orders sau khi có thể đã cập nhật
        orderModels = orderService.getOrdersByUserId(userId);
        List<OrderResponseDto> orders = orderModels.stream()
                .map(order -> mapper.map(order, OrderResponseDto.class))
                .toList();

        return success(orders);
    }

    @GetMapping("get-list-orders-by-user-and-voucher")
    public ResponseEntity<TFUResponse<List<OrderResponseDto>>> getListOrdersByUserIdAndVoucherId(
            @RequestParam Long voucherId) throws Exception {
        Long userId = getCurrentUserId();
        List<OrderModel> orderModels = orderService.getOrdersByUserIdAndVoucherId(userId, voucherId);
        List<OrderResponseDto> orders = orderModels.stream()
                .map(order -> mapper.map(order, OrderResponseDto.class))
                .toList();
        return success(orders);
    }

    @PostMapping("/{orderId}/delivery-status/set")
    public ResponseEntity<TFUResponse<DeliveryStatusDto>> setStep(
            @PathVariable Long orderId,
            @RequestBody DeliveryStatusCreateDto body
    ) {
        var dto = deliveryStatusService.setCurrentStepCascading(
                orderId,
                body.getStep(),
                body.getNote(),
                body.getLocation(),
                body.getImageUrl(),
                getCurrentUserId()
                );
        return success(dto);
    }

    @PutMapping("/{orderId}/delivery-status/{deliveryStatusId}/image")
    public ResponseEntity<TFUResponse<String>> updateDeliveryStatusImage(
            @PathVariable Long orderId,
            @PathVariable Long deliveryStatusId,
            @RequestBody Map<String, String> body
    ) {
        try {
            String imageUrl = body.get("imageUrl");
            deliveryStatusService.updateDeliveryStatusImage(deliveryStatusId, imageUrl);
            return success("Cập nhật hình ảnh thành công");
        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    /// Làm sao để khi người dùng tt thành công thì hệ thống biết -> webhook
    /// Làm sao để đảm bảo là webhook sẽ luôn đáng tin cậy
    /// Khi check-sum key nó bị lộ thì sao. Payos -> trả về cho mình -> mình gọi thẳng lại cho payos để hỏi

    @PostMapping("/webhook-payos")
    public ResponseEntity<TFUResponse<String>> handleWebhook(@RequestBody String rawJson){
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Webhook webhook = objectMapper.readValue(rawJson, Webhook.class);
//            WebhookData data = payOS.webhooks().verify(webhook);
            WebhookData data = webhook.getData();

            if ("00".equals(data.getCode())) {
                String orderCode = String.valueOf(data.getOrderCode());
                // Lấy thông tin thanh toán từ PayOS -> chủ động gọi lại -> đây có gọi là cơ chế polling để đảm bảo giao dịch sẽ luôn đúng (ko bị fake data)
                PaymentLink paymentInfo = payOS.paymentRequests().get(data.getOrderCode());
                if ("PAID".equals(paymentInfo.getStatus().toString())) {
                    orderService.handlePaymentSuccess(String.valueOf(orderCode));
                }
//                orderService.handlePaymentSuccess(String.valueOf(orderCode));
            }
            return success(data.getCode());
        }
        catch (Exception e){
            return success("ok");
        }
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<TFUResponse<String>> cancelOrder(
            @PathVariable Long orderId,
            @RequestBody base.api.dto.request.CancelOrderDto dto
    ) {
        try {
            Long userId = getCurrentUserId();
            orderService.cancelOrder(orderId, userId, dto.getReason());
            return success("Đơn hàng đã được hủy thành công. Yêu cầu hoàn tiền đang được xử lý.");
        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    @GetMapping("/refund-requests")
    public ResponseEntity<TFUResponse<List<base.api.dto.response.RefundRequestDto>>> getRefundRequests() {
        List<base.api.dto.response.RefundRequestDto> refunds = orderService.getAllRefundRequests();
        return success(refunds);
    }

    @GetMapping("/my-refund-requests")
    public ResponseEntity<TFUResponse<List<base.api.dto.response.RefundRequestDto>>> getMyRefundRequests() {
        Long userId = getCurrentUserId();
        List<base.api.dto.response.RefundRequestDto> refunds = orderService.getUserRefundRequests(userId);
        return success(refunds);
    }

    @PostMapping("/refund-requests/{refundId}/process")
    public ResponseEntity<TFUResponse<String>> processRefund(
            @PathVariable Long refundId,
            @RequestBody base.api.dto.request.ProcessRefundDto dto
    ) {
        try {
            Long adminId = getCurrentUserId();
            orderService.processRefund(refundId, adminId, dto);
            return success("Xử lý yêu cầu hoàn tiền thành công");
        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    @PutMapping("/{orderId}/request-delivery-time")
    public ResponseEntity<TFUResponse<String>> updateRequestDeliveryTime(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> body
    ) {
        try {
            String requestDeliveryTimeStr = body.get("requestDeliveryTime");
            if (requestDeliveryTimeStr == null || requestDeliveryTimeStr.isEmpty()) {
                return badRequest("requestDeliveryTime is required");
            }
            
            java.time.LocalDateTime requestDeliveryTime = java.time.LocalDateTime.parse(requestDeliveryTimeStr);
            orderService.updateRequestDeliveryTime(orderId, requestDeliveryTime);
            return success("Cập nhật thời gian giao hàng thành công");
        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

}
