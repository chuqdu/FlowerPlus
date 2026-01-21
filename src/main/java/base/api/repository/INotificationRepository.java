package base.api.repository;

import base.api.entity.NotificationModel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface INotificationRepository extends JpaRepository<NotificationModel, Long> {
    
    List<NotificationModel> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<NotificationModel> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    
    long countByUserIdAndIsReadFalse(Long userId);
    
    List<NotificationModel> findByUserIdAndIsReadFalse(Long userId, Pageable pageable);
}

