package base.api.dto.response;

import base.api.enums.NotificationType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationDto {
    private Long id;
    private String title;
    private String message;
    private NotificationType type;
    private boolean isRead;
    private Long orderId;
    private String orderCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

