package base.api.repository;

import base.api.entity.CategoryModel;
import base.api.enums.SyncStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICategoryRepository extends JpaRepository<CategoryModel, Long>, JpaSpecificationExecutor<CategoryModel> {
    List<CategoryModel> findBySyncStatusOrderByUpdatedAtAsc(SyncStatus syncStatus, Pageable pageable);
    List<CategoryModel> findBySyncStatusInOrderByUpdatedAtAsc(List<SyncStatus> syncStatuses, Pageable pageable);
    List<CategoryModel> findBySyncStatusIsNull();
    long countBySyncStatus(SyncStatus syncStatus);
    
    @Query("SELECT c FROM CategoryModel c WHERE c.syncStatus != :syncedStatus " +
           "ORDER BY c.updatedAt ASC")
    List<CategoryModel> findCategoriesForSync(
        @Param("syncedStatus") SyncStatus syncedStatus,
        Pageable pageable);
}
