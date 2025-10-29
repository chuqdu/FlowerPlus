package com.capstone.BEApp.controller;

import com.capstone.BEApp.dto.common.ResponseDto;
import com.capstone.BEApp.dto.product.LinkProductRequestDto;
import com.capstone.BEApp.dto.product.ProductRequestDto;
import com.capstone.BEApp.dto.product.ProductResponseDto;
import com.capstone.BEApp.entity.enumFile.ProductType;
import com.capstone.BEApp.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseDto<ProductResponseDto> createProduct(@RequestBody ProductRequestDto requestDto) {
        try {
            ProductResponseDto created = productService.createProduct(requestDto);
            return ResponseDto.success(created, "Tạo product thành công");
        } catch (Exception e) {
            return ResponseDto.fail("Tạo product thất bại: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseDto<ProductResponseDto> updateProduct(@PathVariable Long id,
                                                         @RequestBody ProductRequestDto requestDto) {
        try {
            ProductResponseDto updated = productService.updateProduct(id, requestDto);
            return ResponseDto.success(updated, "Cập nhật product thành công");
        } catch (RuntimeException e) {
            return ResponseDto.fail("Cập nhật thất bại: " + e.getMessage());
        } catch (Exception e) {
            return ResponseDto.fail("Cập nhật thất bại: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseDto<ProductResponseDto> getProduct(@PathVariable Long id) {
        try {
            ProductResponseDto product = productService.getProduct(id);
            return ResponseDto.success(product, "Lấy product thành công");
        } catch (RuntimeException e) {
            return ResponseDto.fail("Không tìm thấy product: " + e.getMessage());
        } catch (Exception e) {
            return ResponseDto.fail("Lấy product thất bại: " + e.getMessage());
        }
    }

    @GetMapping("/by-flower-category")
    public ResponseDto<List<ProductResponseDto>> getProductsByFlowerCategory(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Page<ProductResponseDto> result = productService.getProductsByFlowerCategory(categoryId, keyword, page - 1, size);

            return ResponseDto.successWithPagination(
                    result.getContent(),
                    "Lấy danh sách bó hoa theo danh mục hoa thành công",
                    result
            );

        } catch (Exception e) {
            return ResponseDto.fail("Không thể lấy danh sách sản phẩm: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseDto<List<ProductResponseDto>> searchProductsWithType(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false) ProductType type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Page<ProductResponseDto> result = productService.searchProductsWithType(keyword, type, page - 1, size);
            return ResponseDto.successWithPagination(
                    result.getContent(),
                    "Tìm kiếm sản phẩm thành công",
                    result
            );
        } catch (Exception e) {
            return ResponseDto.fail("Không thể tìm kiếm sản phẩm: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseDto<List<ProductResponseDto>> getAllProducts() {
        try {
            List<ProductResponseDto> list = productService.getAllActiveProducts();
            return ResponseDto.success(list, "Lấy danh sách products thành công");
        } catch (Exception e) {
            return ResponseDto.fail("Lấy danh sách thất bại: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseDto<String> softDelete(@PathVariable Long id) {
        try {
            productService.softDeleteProduct(id);
            return ResponseDto.success("OK", "Xóa tạm (soft) product thành công");
        } catch (RuntimeException e) {
            return ResponseDto.fail("Xóa tạm thất bại: " + e.getMessage());
        } catch (Exception e) {
            return ResponseDto.fail("Xóa tạm thất bại: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}/hard")
    public ResponseDto<String> hardDelete(@PathVariable Long id) {
        try {
            productService.hardDeleteProduct(id);
            return ResponseDto.success("OK", "Xóa vĩnh viễn (hard) product thành công");
        } catch (RuntimeException e) {
            return ResponseDto.fail("Xóa vĩnh viễn thất bại: " + e.getMessage());
        } catch (Exception e) {
            return ResponseDto.fail("Xóa vĩnh viễn thất bại: " + e.getMessage());
        }
    }

    @PostMapping("/link")
    public ResponseDto<String> linkProducts(@RequestBody LinkProductRequestDto request) {
        try {
            productService.linkProducts(request);
            return ResponseDto.success(null,"Liên kết giỏ hoa và hoa con thành công");
        } catch (RuntimeException e) {
            return ResponseDto.fail("Liên kết thất bại: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.fail("Đã xảy ra lỗi không mong muốn khi liên kết sản phẩm");
        }
    }
}
