package base.api.repository;

import base.api.entity.CategoryModel;
import base.api.entity.ProductModel;
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
    
    @Query("SELECT DISTINCT p FROM ProductModel p " +
           "LEFT JOIN FETCH p.productCategories pc " +
           "LEFT JOIN FETCH pc.category " +
           "WHERE p.id = :id")
    Optional<ProductModel> findByIdWithCategories(@Param("id") Long id);
    
    @Query("SELECT DISTINCT c FROM ProductCompositionModel comp " +
           "JOIN comp.child child " +
           "JOIN child.productCategories pc " +
           "JOIN pc.category c " +
           "WHERE comp.parent.id = :productId")
    List<CategoryModel> findCategoriesByChildProducts(@Param("productId") Long productId);
}