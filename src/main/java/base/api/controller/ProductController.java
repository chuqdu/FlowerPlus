package base.api.controller;

import base.api.base.BaseAPIController;
import base.api.dto.request.ProductDto;
import base.api.dto.request.paging.PageResponseDTO;
import base.api.dto.request.paging.PageableRequestDTO;
import base.api.dto.response.ProductResponse;
import base.api.dto.response.TFUResponse;
import base.api.entity.ProductModel;
import base.api.enums.ProductType;
import base.api.service.IProductService;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController extends BaseAPIController {

    @Autowired
    private IProductService productService;

    @PostMapping("create-product")
    public ResponseEntity<TFUResponse<ProductModel>> createProduct(@RequestBody ProductDto dto) {
        dto.setUserId(getCurrentUserId());
        ProductModel productModel = productService.createProduct(dto);
        if (productModel == null) {
            return badRequest("Không tạo được product");
        }
        return success(productModel);
    }

    @PutMapping("update-product")
    public ResponseEntity<TFUResponse<ProductModel>> updateProduct(@RequestBody ProductDto dto) {
        dto.setUserId(getCurrentUserId());
        ProductModel productModel = productService.updateProduct(dto);
        if (productModel == null) {
            return badRequest("Không tìm thấy product");
        }
        return success(productModel);
    }

    @GetMapping("get-list-product")
    public ResponseEntity<TFUResponse<PageResponseDTO<ProductResponse>>> getProducts(
            @RequestParam(value = "type", required = false) ProductType type,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "custom", required = false) boolean custom,

            PageableRequestDTO pageableRequest) {
        Long userId = getCurrentUserId();
        Page<ProductResponse> page = productService.getProducts(type, active, categoryId, custom, userId,
                pageableRequest);
        return successPage(page);
    }

    @GetMapping("get-list-product-by-user")
    public ResponseEntity<TFUResponse<PageResponseDTO<ProductResponse>>> getProductsByUser(
            @RequestParam(value = "type", required = false) ProductType type,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "custom", required = false) boolean custom,
            PageableRequestDTO pageableRequest) {
        Long userId = getCurrentUserId();
        Page<ProductResponse> page = productService.getProducts(type, active, categoryId, custom, userId,
                pageableRequest);
        return successPage(page);
    }

    @GetMapping("get-list-product-view")
    public ResponseEntity<TFUResponse<PageResponseDTO<ProductResponse>>> getProductsToView(
            @RequestParam(value = "type", required = false) ProductType type,
            @RequestParam(value = "active", required = false) Boolean active,
            PageableRequestDTO pageableRequest) {
        Page<ProductResponse> page = productService.getProducts(type, active, null, false, 0L,
                pageableRequest);
        return successPage(page);
    }

    @GetMapping("get-product-by-id")
    public ResponseEntity<TFUResponse<ProductResponse>> getProductById(@RequestParam Long id) {
        ProductResponse productModel = productService.getProductById(id);
        if (productModel == null) {
            return badRequest("Không tìm thấy product");
        }
        return success(productModel);
    }

    @DeleteMapping("delete-product")
    public ResponseEntity<TFUResponse<String>> deleteProduct(@RequestParam Long id) {
        String result = productService.deleteProduct(id);
        return success(result);
    }

}
