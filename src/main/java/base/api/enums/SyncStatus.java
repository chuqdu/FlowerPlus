package base.api.enums;

public enum SyncStatus {
    PENDING,    // Chưa sync
    SYNCING,    // Đang sync
    SYNCED,     // Đã sync thành công
    FAILED      // Sync thất bại
}