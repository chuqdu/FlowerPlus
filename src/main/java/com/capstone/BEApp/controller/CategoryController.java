package com.capstone.BEApp.controller;

import com.capstone.BEApp.dto.category.CategoryDto;
import com.capstone.BEApp.dto.common.ResponseDto;
import com.capstone.BEApp.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ResponseDto<CategoryDto>> create(@RequestBody CategoryDto dto) {
        try {
            CategoryDto created = categoryService.create(dto);
            return ResponseEntity.ok(ResponseDto.success(created, "Tạo danh mục thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ResponseDto.fail("Lỗi khi tạo danh mục: " + e.getMessage()));
        }
    }

    @PutMapping("update")
    public ResponseEntity<ResponseDto<CategoryDto>> update(@RequestBody CategoryDto dto) {
        try {
            CategoryDto updated = categoryService.update( dto);
            return ResponseEntity.ok(ResponseDto.success(updated, "Cập nhật danh mục thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ResponseDto.fail("Lỗi khi cập nhật danh mục: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<CategoryDto>> getById(@PathVariable Long id) {
        try {
            CategoryDto category = categoryService.getById(id);
            return ResponseEntity.ok(ResponseDto.success(category, "Lấy thông tin danh mục thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ResponseDto.fail("Không tìm thấy danh mục: " + e.getMessage()));
        }
    }

    @GetMapping("search")
    public ResponseDto<List<CategoryDto>> search(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Page<CategoryDto> result = categoryService.search(keyword, page, size);
            return ResponseDto.successWithPagination(result.getContent(), "Lấy danh sách danh mục thành công", result);
        } catch (Exception e) {
            return ResponseDto.fail("Lỗi khi tìm kiếm danh mục: " + e.getMessage());
        }
    }
}
