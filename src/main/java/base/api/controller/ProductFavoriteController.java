package base.api.controller;

import base.api.base.BaseAPIController;
import base.api.dto.request.FavoriteDto;
import base.api.dto.request.paging.PageResponseDTO;
import base.api.dto.request.paging.PageableRequestDTO;
import base.api.dto.response.FavoriteStatusResponse;
import base.api.dto.response.ProductResponse;
import base.api.dto.response.TFUResponse;
import base.api.service.IProductFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Favorites", description = "API for managing product favorites")
public class ProductFavoriteController extends BaseAPIController {

    private final IProductFavoriteService favoriteService;

    @PostMapping("/toggle")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Toggle favorite status for a product")
    public ResponseEntity<TFUResponse<FavoriteStatusResponse>> toggleFavorite(@RequestBody FavoriteDto dto) {
        try {
            Long userId = getCurrentUserId();
            log.info("User {} toggling favorite for product {}", userId, dto.getProductId());
            
            boolean isFavorited = favoriteService.toggleFavorite(userId, dto.getProductId());
            
            FavoriteStatusResponse response = new FavoriteStatusResponse(dto.getProductId(), isFavorited);
            
            String message = isFavorited ? "Đã thêm vào yêu thích" : "Đã xóa khỏi yêu thích";
            return success(response, message);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid product for favorite toggle: {}", e.getMessage());
            return badRequest("Sản phẩm không tồn tại hoặc không khả dụng");
        } catch (Exception e) {
            log.error("Error toggling favorite", e);
            return badRequest("Có lỗi xảy ra khi cập nhật yêu thích");
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get user's favorite products with pagination")
    public ResponseEntity<TFUResponse<PageResponseDTO<ProductResponse>>> getUserFavorites(
            PageableRequestDTO pageableRequest) {
        try {
            Long userId = getCurrentUserId();
            log.info("Getting favorites for user {} with pagination: page={}, size={}", 
                    userId, pageableRequest.getPageNumber(), pageableRequest.getPageSize());
            
            Page<ProductResponse> favoritePage = favoriteService.getUserFavorites(userId, pageableRequest);
            
            return successPage(favoritePage);
            
        } catch (Exception e) {
            log.error("Error getting user favorites", e);
            return badRequest("Có lỗi xảy ra khi lấy danh sách yêu thích");
        }
    }

    @GetMapping("/check/{productId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Check if a product is favorited by current user")
    public ResponseEntity<TFUResponse<FavoriteStatusResponse>> checkFavoriteStatus(
            @Parameter(description = "Product ID to check") @PathVariable Long productId) {
        try {
            Long userId = getCurrentUserId();
            log.info("Checking favorite status for user {} and product {}", userId, productId);
            
            boolean isFavorited = favoriteService.isFavorited(userId, productId);
            FavoriteStatusResponse response = new FavoriteStatusResponse(productId, isFavorited);
            
            return success(response);
            
        } catch (Exception e) {
            log.error("Error checking favorite status", e);
            return error("Có lỗi xảy ra khi kiểm tra trạng thái yêu thích", org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/check-multiple")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Check favorite status for multiple products")
    public ResponseEntity<TFUResponse<Map<Long, Boolean>>> checkMultipleFavoriteStatus(
            @RequestBody List<Long> productIds) {
        try {
            Long userId = getCurrentUserId();
            log.info("Checking favorite status for user {} and {} products", userId, productIds.size());
            
            Map<Long, Boolean> statusMap = favoriteService.getFavoriteStatusMap(userId, productIds);
            
            return success(statusMap);
            
        } catch (Exception e) {
            log.error("Error checking multiple favorite status", e);
            return error("Có lỗi xảy ra khi kiểm tra trạng thái yêu thích", org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Remove a product from favorites")
    public ResponseEntity<TFUResponse<String>> removeFavorite(
            @Parameter(description = "Product ID to remove from favorites") @PathVariable Long productId) {
        try {
            Long userId = getCurrentUserId();
            log.info("User {} removing product {} from favorites", userId, productId);
            
            favoriteService.removeFavorite(userId, productId);
            
            return success("Đã xóa khỏi danh sách yêu thích");
            
        } catch (Exception e) {
            log.error("Error removing favorite", e);
            return error("Có lỗi xảy ra khi xóa yêu thích", org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/count/{productId}")
    @Operation(summary = "Get favorite count for a product (public endpoint)")
    public ResponseEntity<TFUResponse<Long>> getFavoriteCount(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        try {
            log.info("Getting favorite count for product {}", productId);
            
            long count = favoriteService.getFavoriteCount(productId);
            
            return success(count);
            
        } catch (Exception e) {
            log.error("Error getting favorite count", e);
            return error("Có lỗi xảy ra khi lấy số lượng yêu thích", org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Admin endpoints for analytics
    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get favorite statistics for products (Admin only)")
    public ResponseEntity<TFUResponse<Map<Long, Long>>> getFavoriteStatistics(
            @RequestParam List<Long> productIds) {
        try {
            log.info("Admin getting favorite statistics for {} products", productIds.size());
            
            // This would need to be added to the service interface
            // Map<Long, Long> statistics = favoriteService.getFavoriteStatistics(productIds);
            
            return success(Map.of()); // Placeholder
            
        } catch (Exception e) {
            log.error("Error getting favorite statistics", e);
            return error("Có lỗi xảy ra khi lấy thống kê yêu thích", org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}