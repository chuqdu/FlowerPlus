package base.api.controller;

import base.api.base.BaseAPIController;
import base.api.dto.request.OrderDto;
import base.api.dto.response.OrderResponseDto;
import base.api.dto.response.TFUResponse;
import base.api.entity.OrderModel;
import base.api.service.IOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.type.*;

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
    ModelMapper mapper;

    @PostMapping("checkout")
    public ResponseEntity<TFUResponse<Map<String, Object>>> checkout() throws Exception {
        Long userId = getCurrentUserId();
        String returnUrl = "https://gooogle.com";
        String cancelUrl = "https://gooogle.com";
        String result = orderService.checkout(userId, returnUrl, cancelUrl);
        Map<String, Object> data = new HashMap<>();
        data.put("checkoutUrl", result);
        return success(data);
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



    @PostMapping("/webhook-payos")
    public ResponseEntity<TFUResponse<String>> handleWebhook(@RequestBody String rawJson){
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Webhook webhook = objectMapper.readValue(rawJson, Webhook.class);
            WebhookData data = payOS.verifyPaymentWebhookData(webhook);
            return success(data.getCode());
        }
        catch (Exception e){
            return badRequest(e.getMessage());
        }
    }


}
