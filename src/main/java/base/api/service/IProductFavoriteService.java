package base.api.service;

import base.api.dto.request.paging.PageableRequestDTO;
import base.api.dto.response.FavoriteResponse;
import base.api.dto.response.FavoriteStatusResponse;
import base.api.dto.response.ProductResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface IProductFavoriteService {
    
    /**
     * Toggle favorite status for a product
     * @param userId User ID
     * @param productId Product ID
     * @return true if added to favorites, false if removed
     */
    boolean toggleFavorite(Long userId, Long productId);
    
    /**
     * Get user's favorite products with pagination
     * @param userId User ID
     * @param request Pagination request
     * @return Page of favorite products
     */
    Page<ProductResponse> getUserFavorites(Long userId, PageableRequestDTO request);
    
    /**
     * Check if a product is favorited by user
     * @param userId User ID
     * @param productId Product ID
     * @return true if favorited, false otherwise
     */
    boolean isFavorited(Long userId, Long productId);
    
    /**
     * Remove a product from favorites
     * @param userId User ID
     * @param productId Product ID
     */
    void removeFavorite(Long userId, Long productId);
    
    /**
     * Get favorite status for multiple products
     * @param userId User ID
     * @param productIds List of product IDs
     * @return Map of product ID to favorite status
     */
    Map<Long, Boolean> getFavoriteStatusMap(Long userId, List<Long> productIds);
    
    /**
     * Get favorite count for a product
     * @param productId Product ID
     * @return Number of users who favorited this product
     */
    long getFavoriteCount(Long productId);
}