package base.api.controller;

import base.api.base.BaseAPIController;
import base.api.dto.request.AiCreateOrderDto;
import base.api.dto.response.TFUResponse;
import base.api.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/orders")
public class AiOrderController extends BaseAPIController {

    @Autowired
    private IOrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<TFUResponse<Map<String, Object>>> createOrder(@RequestBody AiCreateOrderDto dto) {
        try {
            // Validate input
            if (dto.getProductId() == null || dto.getUserId() == null) {
                return badRequest("productId and userId are required");
            }

            // Tạo đơn hàng và lấy link thanh toán
            String checkoutUrl = orderService.createOrderForAi(dto);
            
            // Trả về link thanh toán
            Map<String, Object> data = new HashMap<>();
            data.put("checkoutUrl", checkoutUrl);
            
            return success(data);
        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }
}

