package base.api.repository;

import base.api.entity.CategoryModel;
import base.api.entity.ProductModel;
import base.api.enums.ProductType;
import base.api.enums.SyncStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IProductRepository extends JpaRepository<ProductModel, Long>, JpaSpecificationExecutor<ProductModel> {
    List<ProductModel> findBySyncStatusOrderByUpdatedAtAsc(SyncStatus syncStatus, Pageable pageable);
    List<ProductModel> findBySyncStatusInOrderByUpdatedAtAsc(List<SyncStatus> syncStatuses, Pageable pageable);
    List<ProductModel> findBySyncStatusIsNullOrProductStringIsNull();
    long countBySyncStatus(SyncStatus syncStatus);
    
    // Query for syncing: only active products with price > 0
    @Query("SELECT p FROM ProductModel p WHERE p.syncStatus IN :syncStatuses " +
           "AND p.isActive = true AND p.price > 0 " +
           "ORDER BY p.updatedAt ASC")
    List<ProductModel> findActiveProductsWithPriceForSync(
        @Param("syncStatuses") List<SyncStatus> syncStatuses, 
        Pageable pageable);
    
    // Query for syncing failed PRODUCT type: only active products with price > 0, type = PRODUCT, sync status = FAILED
    @Query("SELECT p FROM ProductModel p WHERE p.syncStatus = :syncStatus " +
           "AND p.isActive = true AND p.price > 0 AND p.productType = :productType " +
           "ORDER BY p.updatedAt DESC")
    List<ProductModel> findFailedProductsForSync(
        @Param("syncStatus") SyncStatus syncStatus,
        @Param("productType") ProductType productType,
        Pageable pageable);
    
    @Query("SELECT DISTINCT p FROM ProductModel p " +
           "LEFT JOIN FETCH p.productCategories pc " +
           "LEFT JOIN FETCH pc.category " +
           "WHERE p.id = :id")
    Optional<ProductModel> findByIdWithCategories(@Param("id") Long id);
    
    @Query("SELECT p FROM ProductModel p WHERE p.syncStatus != :syncedStatus " +
           "AND p.productType = :productType " +
           "AND p.isActive = true AND p.price > 0 " +
           "ORDER BY p.updatedAt ASC")
    List<ProductModel> findProductsForSync(
        @Param("syncedStatus") SyncStatus syncedStatus,
        @Param("productType") ProductType productType,
        Pageable pageable);
    
}