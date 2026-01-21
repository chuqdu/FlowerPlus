package base.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {
    NEW_ORDER,
    ORDER_UPDATED,
    ORDER_CANCELLED
}

