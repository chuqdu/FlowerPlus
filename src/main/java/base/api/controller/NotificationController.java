package base.api.controller;

import base.api.base.BaseAPIController;
import base.api.dto.response.NotificationDto;
import base.api.dto.response.TFUResponse;
import base.api.entity.NotificationModel;
import base.api.service.INotificationDbService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController extends BaseAPIController {

    @Autowired
    private INotificationDbService notificationDbService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<TFUResponse<List<NotificationDto>>> getNotifications() {
        Long userId = getCurrentUserId();
        List<NotificationModel> notifications = notificationDbService.getUserNotifications(userId);
        List<NotificationDto> dtos = notifications.stream()
                .map(notification -> modelMapper.map(notification, NotificationDto.class))
                .collect(Collectors.toList());
        return success(dtos);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<TFUResponse<Long>> getUnreadCount() {
        Long userId = getCurrentUserId();
        long count = notificationDbService.getUnreadCount(userId);
        return success(count);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<TFUResponse<String>> markAsRead(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        notificationDbService.markAsRead(id, userId);
        return success("Notification marked as read");
    }

    @PutMapping("/read-all")
    public ResponseEntity<TFUResponse<String>> markAllAsRead() {
        Long userId = getCurrentUserId();
        notificationDbService.markAllAsRead(userId);
        return success("All notifications marked as read");
    }
}

