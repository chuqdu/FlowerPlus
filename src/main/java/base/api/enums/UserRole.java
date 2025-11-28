package base.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRole {
    USER,
    SHOP_OWNER,
    ADMIN,
    DELIVERY_PERSON;
}
