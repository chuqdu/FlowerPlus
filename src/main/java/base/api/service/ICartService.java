package base.api.service;

import base.api.dto.request.CartDto;
import base.api.dto.request.CartItemRequest;
import base.api.dto.request.CartItemUpdateRequest;
import base.api.dto.request.CartResponse;
import base.api.entity.CartModel;

public interface ICartService {
    CartResponse getCartByUser(Long userId);
    CartResponse addItem(Long userId, CartItemRequest request);
    CartResponse updateItem(Long userId, Long cartItemId, CartItemUpdateRequest request);
    CartResponse removeItem(Long userId, Long cartItemId);
    CartResponse clearCart(Long userId);
}
