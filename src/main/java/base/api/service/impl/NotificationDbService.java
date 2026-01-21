package base.api.service.impl;

import base.api.entity.NotificationModel;
import base.api.enums.NotificationType;
import base.api.repository.INotificationRepository;
import base.api.service.INotificationDbService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationDbService implements INotificationDbService {

    @Autowired
    private INotificationRepository notificationRepository;

    @Override
    @Transactional
    public NotificationModel createNotification(Long userId, String title, String message, NotificationType type, Long orderId, String orderCode) {
        NotificationModel notification = new NotificationModel();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setOrderId(orderId);
        notification.setOrderCode(orderCode);
        notification.setRead(false);
        
        return notificationRepository.save(notification);
    }

    @Override
    public List<NotificationModel> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<NotificationModel> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        NotificationModel notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
        
        // Kiểm tra notification thuộc về user
        if (!notification.getUserId().equals(userId)) {
            throw new IllegalStateException("Notification does not belong to user");
        }
        
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        List<NotificationModel> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        for (NotificationModel notification : unreadNotifications) {
            notification.setRead(true);
        }
        notificationRepository.saveAll(unreadNotifications);
    }
}

