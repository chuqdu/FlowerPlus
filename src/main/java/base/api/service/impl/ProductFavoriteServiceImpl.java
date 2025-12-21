package base.api.service.impl;

import base.api.dto.request.paging.PageableRequestDTO;
import base.api.dto.response.FavoriteResponse;
import base.api.dto.response.ProductResponse;
import base.api.entity.ProductFavoriteModel;
import base.api.entity.ProductModel;
import base.api.repository.IProductRepository;
import base.api.repository.ProductFavoriteRepository;
import base.api.service.IProductFavoriteService;
import base.api.service.IProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductFavoriteServiceImpl implements IProductFavoriteService {

    private final ProductFavoriteRepository favoriteRepository;
    private final IProductRepository productRepository;
    private final IProductService productService;

    @Override
    public boolean toggleFavorite(Long userId, Long productId) {
        log.info("Toggling favorite for user {} and product {}", userId, productId);
        
        // Validate product exists and is active
        Optional<ProductModel> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty() || !productOpt.get().getIsActive()) {
            throw new IllegalArgumentException("Product not found or inactive: " + productId);
        }

        Optional<ProductFavoriteModel> existingFavorite = 
            favoriteRepository.findByUserIdAndProductId(userId, productId);

        if (existingFavorite.isPresent()) {
            // Remove from favorites
            favoriteRepository.delete(existingFavorite.get());
            log.info("Removed product {} from favorites for user {}", productId, userId);
            return false;
        } else {
            // Add to favorites
            ProductFavoriteModel favorite = new ProductFavoriteModel(userId, productId);
            favoriteRepository.save(favorite);
            log.info("Added product {} to favorites for user {}", productId, userId);
            return true;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getUserFavorites(Long userId, PageableRequestDTO request) {
        log.info("Getting favorites for user {} with pagination: page={}, size={}", 
                userId, request.getPageNumber(), request.getPageSize());

        Pageable pageable = PageRequest.of(
            request.getPageNumber(), 
            request.getPageSize()
        );

        Page<ProductFavoriteModel> favoritePage = favoriteRepository.findByUserIdWithProduct(userId, pageable);
        
        return favoritePage.map(favorite -> {
            ProductResponse productResponse = productService.getProductById(favorite.getProductId());
            return productResponse;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorited(Long userId, Long productId) {
        return favoriteRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    public void removeFavorite(Long userId, Long productId) {
        log.info("Removing favorite for user {} and product {}", userId, productId);
        
        Optional<ProductFavoriteModel> favorite = 
            favoriteRepository.findByUserIdAndProductId(userId, productId);
        
        if (favorite.isPresent()) {
            favoriteRepository.delete(favorite.get());
            log.info("Successfully removed favorite for user {} and product {}", userId, productId);
        } else {
            log.warn("Favorite not found for user {} and product {}", userId, productId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Boolean> getFavoriteStatusMap(Long userId, List<Long> productIds) {
        log.info("Getting favorite status map for user {} and {} products", userId, productIds.size());
        
        List<Long> favoritedProductIds = favoriteRepository.findFavoritedProductIds(userId, productIds);
        
        Map<Long, Boolean> statusMap = new HashMap<>();
        for (Long productId : productIds) {
            statusMap.put(productId, favoritedProductIds.contains(productId));
        }
        
        return statusMap;
    }

    @Override
    @Transactional(readOnly = true)
    public long getFavoriteCount(Long productId) {
        return favoriteRepository.countByProductId(productId);
    }

    /**
     * Get recent favorites for a user (helper method)
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getRecentFavorites(Long userId, int limit) {
        log.info("Getting {} recent favorites for user {}", limit, userId);
        
        Pageable pageable = PageRequest.of(0, limit);
        List<ProductFavoriteModel> recentFavorites = favoriteRepository.getRecentFavorites(userId, pageable);
        
        return recentFavorites.stream()
            .map(favorite -> productService.getProductById(favorite.getProductId()))
            .collect(Collectors.toList());
    }

    /**
     * Get favorite statistics for products (helper method for analytics)
     */
    @Transactional(readOnly = true)
    public Map<Long, Long> getFavoriteStatistics(List<Long> productIds) {
        log.info("Getting favorite statistics for {} products", productIds.size());
        
        List<Object[]> statistics = favoriteRepository.getFavoriteStatistics(productIds);
        
        Map<Long, Long> statsMap = new HashMap<>();
        for (Object[] stat : statistics) {
            Long productId = (Long) stat[0];
            Long count = (Long) stat[1];
            statsMap.put(productId, count);
        }
        
        // Fill in zero counts for products with no favorites
        for (Long productId : productIds) {
            statsMap.putIfAbsent(productId, 0L);
        }
        
        return statsMap;
    }
}