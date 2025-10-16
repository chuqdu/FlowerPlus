package com.capstone.BEApp.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplaceCartRequestDto {
    private List<CartItemRequestDto> items;
}