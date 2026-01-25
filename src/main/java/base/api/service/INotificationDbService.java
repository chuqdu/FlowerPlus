package base.api.service;

import base.api.entity.NotificationModel;
import base.api.enums.NotificationType;

import java.util.List;

public interface INotificationDbService {
    
    /**
     * Tạo notification mới
     */
    NotificationModel createNotification(Long userId, String title, String message, NotificationType type, Long orderId, String orderCode);
    
    /**
     * Lấy tất cả notifications của user
     */
    List<NotificationModel> getUserNotifications(Long userId);
    
    /**
     * Lấy danh sách notifications chưa đọc của user
     */
    List<NotificationModel> getUnreadNotifications(Long userId);
    
    /**
     * Lấy số lượng notifications chưa đọc
     */
    long getUnreadCount(Long userId);
    
    /**
     * Đánh dấu notification đã đọc
     */
    void markAsRead(Long notificationId, Long userId);
    
    /**
     * Đánh dấu tất cả notifications của user đã đọc
     */
    void markAllAsRead(Long userId);
}

