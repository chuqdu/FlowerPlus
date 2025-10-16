package com.capstone.BEApp.controller;

import com.capstone.BEApp.dto.common.ResponseDto;
import com.capstone.BEApp.dto.item.ItemDto;
import com.capstone.BEApp.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseDto<ItemDto> create(@RequestBody ItemDto dto) {
        return ResponseDto.success(itemService.create(dto), "Thêm sản phẩm thành công");
    }

    @PutMapping("/{id}")
    public ResponseDto<ItemDto> update(@PathVariable Long id, @RequestBody ItemDto dto) {
        return ResponseDto.success(itemService.update(id, dto), "Cập nhật sản phẩm thành công");
    }

    @GetMapping("/{id}")
    public ResponseDto<ItemDto> getById(@PathVariable Long id) {
        return ResponseDto.success(itemService.getById(id), "Lấy thông tin sản phẩm thành công");
    }

    @GetMapping
    public ResponseDto<?> search(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ItemDto> result = itemService.search(keyword, page, size);
        return ResponseDto.successWithPagination(result.getContent(), "Lấy danh sách sản phẩm thành công", result);
    }
}
