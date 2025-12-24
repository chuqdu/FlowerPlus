package base.api.service.impl;

import base.api.dto.request.CartItemResponse;
import base.api.dto.request.CartResponse;
import base.api.entity.CartItemModel;
import base.api.entity.CartModel;

import java.util.stream.Collectors;

public class CartMapper {

    public static CartItemResponse toItemResponse(CartItemModel e) {
        CartItemResponse r = new CartItemResponse();
        r.setId(e.getId());
        r.setProductId(e.getProductId());
        r.setProductName(e.getProductName());
//        r.setProductImage(e.getProductImage());
        String productImage = e.getProductImage();
        r.setProductImage(productImage != null ? productImage.replace("http://", "https://") : null);
        r.setUnitPrice(e.getUnitPrice());
        r.setQuantity(e.getQuantity());
        r.setLineTotal(e.getLineTotal());
        return r;
    }

    public static CartResponse toCartResponse(CartModel cart) {
        CartResponse r = new CartResponse();
        r.setId(cart.getId());
        r.setUserId(cart.getUser().getId());
        r.setTotalPrice(cart.getTotalPrice());
        r.setItems(cart.getCartItems()
                .stream()
                .map(CartMapper::toItemResponse)
                .collect(Collectors.toList()));
        return r;
    }
}