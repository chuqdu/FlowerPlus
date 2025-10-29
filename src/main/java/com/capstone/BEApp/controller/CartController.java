//package com.capstone.BEApp.controller;
//
//import com.capstone.BEApp.dto.cart.*;
//import com.capstone.BEApp.dto.common.ResponseDto;
//import com.capstone.BEApp.service.CartService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/cart")
//@RequiredArgsConstructor
//public class CartController {
//
//    private final CartService cartService;
//
//    @PostMapping("/{accountId}/replace")
//    public ResponseDto<CartResponseDto> replaceCart(
//            @PathVariable Long accountId,
//            @RequestBody ReplaceCartRequestDto request
//    ) {
//        try {
//            CartResponseDto updatedCart = cartService.replaceCart(accountId, request);
//            return ResponseDto.success(updatedCart, "Đã cập nhật giỏ hàng thành công");
//        } catch (Exception e) {
//            return ResponseDto.fail("Lỗi khi cập nhật giỏ hàng: " + e.getMessage());
//        }
//    }
//}
