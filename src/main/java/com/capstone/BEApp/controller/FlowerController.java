package com.capstone.BEApp.controller;

import com.capstone.BEApp.dto.common.ResponseDto;
import com.capstone.BEApp.dto.flower.CreateFlowerDto;
import com.capstone.BEApp.dto.flower.FlowerDto;
import com.capstone.BEApp.dto.flower.UpdateFlowerDto;
import com.capstone.BEApp.service.FlowerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flowers")
@RequiredArgsConstructor
public class FlowerController {

    private final FlowerService flowerService;

    @PostMapping
    public ResponseDto<FlowerDto> create(@RequestBody CreateFlowerDto dto) {
        try {
            return ResponseDto.success(flowerService.create(dto), "Thêm hoa thành công");
        } catch (Exception e) {
            return ResponseDto.fail("Lỗi khi thêm hoa: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseDto<FlowerDto> update(@PathVariable Long id, @RequestBody UpdateFlowerDto dto) {
        try {
            dto.setId(id); // Giả sử UpdateFlowerDto cần set id
            return ResponseDto.success(flowerService.update(dto), "Cập nhật hoa thành công");
        } catch (Exception e) {
            return ResponseDto.fail("Lỗi khi cập nhật hoa: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseDto<FlowerDto> getById(@PathVariable Long id) {
        try {
            return ResponseDto.success(flowerService.getById(id), "Lấy thông tin hoa thành công");
        } catch (Exception e) {
            return ResponseDto.fail("Lỗi khi lấy thông tin hoa: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseDto<?> search(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Page<FlowerDto> result = flowerService.search(keyword, page - 1, size);
            return ResponseDto.successWithPagination(result.getContent(), "Lấy danh sách hoa thành công", result);
        } catch (Exception e) {
            return ResponseDto.fail("Lỗi khi tìm kiếm hoa: " + e.getMessage());
        }
    }
}
