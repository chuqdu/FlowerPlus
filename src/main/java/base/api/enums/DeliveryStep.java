package base.api.enums;

public enum DeliveryStep {
    PENDING_CONFIRMATION, // chờ xác nhận
    PREPARING,            // đang chuẩn bị
    DELIVERING,           // đang giao
    DELIVERED,            // giao thành công
    DELIVERY_FAILED       // giao thất bại
}