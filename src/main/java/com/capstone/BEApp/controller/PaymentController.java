package com.capstone.BEApp.controller;
import com.capstone.BEApp.dto.common.ResponseDto;
import com.capstone.BEApp.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final VNPayService vnPayService;

    @PostMapping("/create")
    public ResponseDto<String> createPayment(@RequestParam Long accountId,
                                             @RequestParam BigDecimal amount,
                                             HttpServletRequest request) {
        try {
            String paymentUrl = vnPayService.createPaymentUrl(accountId, amount, request);
            return ResponseDto.success(paymentUrl, "Tạo link thanh toán thành công");
        } catch (Exception e) {
            return ResponseDto.fail("Tạo link thanh toán thất bại: " + e.getMessage());
        }
    }

    @GetMapping("/vnpay-return")
    public String handleVNPayReturn(@RequestParam Map<String, String> params) {
        String responseCode = params.get("vnp_ResponseCode");
        if ("00".equals(responseCode)) {
            return "Thanh toán thành công!";
        } else {
            return "Thanh toán thất bại!";
        }
    }
}
