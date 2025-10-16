package com.capstone.BEApp.service;

import com.capstone.BEApp.dto.cart.CartResponseDto;
import com.capstone.BEApp.dto.cart.ReplaceCartRequestDto;

public interface CartService {
    CartResponseDto replaceCart(Long accountId, ReplaceCartRequestDto request);
}
