package base.api.repository;

import base.api.entity.ProductFavoriteModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductFavoriteRepository extends JpaRepository<ProductFavoriteModel, Long> {
    
    /**
     * Find favorite by user and product
     */
    Optional<ProductFavoriteModel> findByUserIdAndProductId(Long userId, Long productId);
    
    /**
     * Check if product is favorited by user
     */
    boolean existsByUserIdAndProductId(Long userId, Long productId);
    
    /**
     * Get user's favorites with pagination
     */
    @Query("SELECT pf FROM ProductFavoriteModel pf " +
           "JOIN FETCH pf.product p " +
           "WHERE pf.userId = :userId AND p.isActive = true " +
           "ORDER BY pf.createdAt DESC")
    Page<ProductFavoriteModel> findByUserIdWithProduct(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Get favorite status for multiple products
     */
    @Query("SELECT pf.productId FROM ProductFavoriteModel pf " +
           "WHERE pf.userId = :userId AND pf.productId IN :productIds")
    List<Long> findFavoritedProductIds(@Param("userId") Long userId, @Param("productIds") List<Long> productIds);
    
    /**
     * Count favorites for a product
     */
    long countByProductId(Long productId);
    
    /**
     * Delete favorite by user and product
     */
    void deleteByUserIdAndProductId(Long userId, Long productId);
    
    /**
     * Delete all favorites for a user (for cascade delete)
     */
    void deleteByUserId(Long userId);
    
    /**
     * Delete all favorites for a product (for cascade delete)
     */
    void deleteByProductId(Long productId);
    
    /**
     * Count favorites by user ID
     */
    long countByUserId(Long userId);
    
    /**
     * Get all favorites for a user (for analytics)
     */
    @Query("SELECT pf FROM ProductFavoriteModel pf WHERE pf.userId = :userId ORDER BY pf.createdAt DESC")
    List<ProductFavoriteModel> findAllByUserId(@Param("userId") Long userId);
    
    /**
     * Get favorite statistics for products
     */
    @Query("SELECT pf.productId, COUNT(pf) FROM ProductFavoriteModel pf " +
           "WHERE pf.productId IN :productIds " +
           "GROUP BY pf.productId")
    List<Object[]> getFavoriteStatistics(@Param("productIds") List<Long> productIds);
    
    /**
     * Get most favorited products
     */
    @Query("SELECT pf.productId, COUNT(pf) as favoriteCount FROM ProductFavoriteModel pf " +
           "JOIN pf.product p WHERE p.isActive = true " +
           "GROUP BY pf.productId " +
           "ORDER BY favoriteCount DESC")
    Page<Object[]> getMostFavoritedProducts(Pageable pageable);
    
    /**
     * Get recent favorites for a user
     */
    @Query("SELECT pf FROM ProductFavoriteModel pf " +
           "JOIN FETCH pf.product p " +
           "WHERE pf.userId = :userId AND p.isActive = true " +
           "ORDER BY pf.createdAt DESC")
    List<ProductFavoriteModel> getRecentFavorites(@Param("userId") Long userId, Pageable pageable);

    Long countByUserIdAndProductId(Long userId, Long productId);

}