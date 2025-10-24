package com.capstone.BEApp.controller;

import com.capstone.BEApp.dto.common.ResponseDto;
import com.capstone.BEApp.dto.item.CreateItemDto;
import com.capstone.BEApp.dto.item.ItemDto;
import com.capstone.BEApp.dto.item.UpdateItemDto;
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
    public ResponseDto<ItemDto> create(@RequestBody CreateItemDto dto) {
        try {
            ItemDto created = itemService.create(dto);
            return ResponseDto.success(created, "Thêm sản phẩm thành công");
        } catch (Exception e) {
            return ResponseDto.fail("Lỗi khi thêm sản phẩm: " + e.getMessage());
        }
    }

    @PutMapping
    public ResponseDto<ItemDto> update(@RequestBody UpdateItemDto dto) {
        try {
            ItemDto updated = itemService.update(dto);
            return ResponseDto.success(updated, "Cập nhật sản phẩm thành công");
        } catch (Exception e) {
            return ResponseDto.fail("Lỗi khi cập nhật sản phẩm: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseDto<ItemDto> getById(@PathVariable Long id) {
        try {
            ItemDto item = itemService.getById(id);
            return ResponseDto.success(item, "Lấy thông tin sản phẩm thành công");
        } catch (Exception e) {
            return ResponseDto.fail("Lỗi khi lấy thông tin sản phẩm: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseDto<?> search(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Page<ItemDto> result = itemService.search(keyword, page - 1, size);
            return ResponseDto.successWithPagination(
                    result.getContent(),
                    "Lấy danh sách sản phẩm thành công",
                    result
            );
        } catch (Exception e) {
            return ResponseDto.fail("Lỗi khi tìm kiếm sản phẩm: " + e.getMessage());
        }
    }

}
