package com.capstone.BEApp.service.impl;

import com.capstone.BEApp.dto.cart.*;
import com.capstone.BEApp.entity.*;
import com.capstone.BEApp.repository.*;
import com.capstone.BEApp.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;

    @Override
    public CartResponseDto replaceCart(Long accountId, ReplaceCartRequestDto request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản"));

        cartRepository.findByAccount(account).ifPresent(cartRepository::delete);

        Cart newCart = Cart.builder()
                .account(account)
                .totalPrice(BigDecimal.ZERO)
                .build();
        cartRepository.save(newCart);

        BigDecimal total = BigDecimal.ZERO;
        for (CartItemRequestDto itemDto : request.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm ID " + itemDto.getProductId()));

            BigDecimal itemTotal = product.getProductPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            total = total.add(itemTotal);

            CartItem item = CartItem.builder()
                    .cart(newCart)
                    .product(product)
                    .productName(product.getName())
                    .productPrice(product.getProductPrice())
                    .quantity(itemDto.getQuantity())
                    .build();

            cartItemRepository.save(item);
        }

        newCart.setTotalPrice(total);
        cartRepository.save(newCart);

        List<CartItemDto> itemDtos = newCart.getCartItems().stream()
                .map(i -> CartItemDto.builder()
                        .id(i.getId())
                        .productName(i.getProductName())
                        .productPrice(i.getProductPrice())
                        .quantity(i.getQuantity())
                        .build())
                .collect(Collectors.toList());

        return CartResponseDto.builder()
                .cartId(newCart.getId())
                .totalPrice(total)
                .items(itemDtos)
                .build();
    }
}
