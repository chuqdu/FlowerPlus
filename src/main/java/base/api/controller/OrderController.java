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
        String returnUrl = "https://gooogle.com";
        String cancelUrl = "https://gooogle.com";
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
        String returnUrl = "https://flower-plus.vercel.app/profile";
        String cancelUrl = "https://flower-plus.vercel.app/profile";
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
        String returnUrl = "https://flower-plus.vercel.app/profile";
        String cancelUrl = "https://flower-plus.vercel.app/profile";
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

    @PostMapping("/webhook-payos")
    public ResponseEntity<TFUResponse<String>> handleWebhook(@RequestBody String rawJson){
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Webhook webhook = objectMapper.readValue(rawJson, Webhook.class);
//            WebhookData data = payOS.webhooks().verify(webhook);
            WebhookData data = webhook.getData();
            if ("00".equals(data.getCode())) {
                String orderCode = String.valueOf(data.getOrderCode());
                orderService.handlePaymentSuccess(orderCode);
            }
            return success(data.getCode());
        }
        catch (Exception e){
            return success("ok");
        }
    }


}
