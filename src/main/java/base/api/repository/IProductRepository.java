package base.api.repository;

import base.api.entity.ProductModel;
import base.api.enums.SyncStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IProductRepository extends JpaRepository<ProductModel, Long>, JpaSpecificationExecutor<ProductModel> {
    List<ProductModel> findBySyncStatusOrderByUpdatedAtAsc(SyncStatus syncStatus, Pageable pageable);
    List<ProductModel> findBySyncStatusInOrderByUpdatedAtAsc(List<SyncStatus> syncStatuses, Pageable pageable);
    List<ProductModel> findBySyncStatusIsNullOrProductStringIsNull();
    long countBySyncStatus(SyncStatus syncStatus);
}