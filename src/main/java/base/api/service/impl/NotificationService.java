package base.api.service.impl;

import base.api.entity.UserVoucherModel;
import base.api.service.INotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService implements INotificationService {

    @Override
    public void sendVoucherAssignmentNotification(UserVoucherModel userVoucher) {
        if (!isVoucherNotificationEnabled(userVoucher.getUser().getId())) {
            log.debug("Voucher notifications disabled for user {}", userVoucher.getUser().getId());
            return;
        }

        try {
            // TODO: Implement actual notification sending logic
            // This could be email, push notification, in-app notification, etc.
            
            String message = String.format(
                "Bạn đã nhận được voucher cá nhân mới: %s. " +
                "Voucher có giá trị %s và có thể sử dụng từ %s đến %s.",
                userVoucher.getVoucher().getCode(),
                formatVoucherValue(userVoucher.getVoucher()),
                formatDateTime(userVoucher.getVoucher().getStartsAt()),
                formatDateTime(userVoucher.getVoucher().getEndsAt())
            );
            
            log.info("Sending voucher assignment notification to user {}: {}", 
                    userVoucher.getUser().getId(), message);
            
            // Simulate notification sending
            sendNotificationToUser(userVoucher.getUser().getId(), "Voucher mới", message);
            
        } catch (Exception e) {
            log.error("Failed to send voucher assignment notification to user {}: {}", 
                     userVoucher.getUser().getId(), e.getMessage(), e);
        }
    }

    @Override
    public void sendVoucherUsageConfirmation(UserVoucherModel userVoucher) {
        if (!isVoucherNotificationEnabled(userVoucher.getUser().getId())) {
            return;
        }

        try {
            String message = String.format(
                "Voucher %s đã được sử dụng thành công vào lúc %s. " +
                "Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi!",
                userVoucher.getVoucher().getCode(),
                formatDateTime(userVoucher.getUsedAt())
            );
            
            log.info("Sending voucher usage confirmation to user {}: {}", 
                    userVoucher.getUser().getId(), message);
            
            sendNotificationToUser(userVoucher.getUser().getId(), "Voucher đã sử dụng", message);
            
        } catch (Exception e) {
            log.error("Failed to send voucher usage confirmation to user {}: {}", 
                     userVoucher.getUser().getId(), e.getMessage(), e);
        }
    }

    @Override
    public void sendVoucherExpirationReminder(UserVoucherModel userVoucher) {
        if (!isVoucherNotificationEnabled(userVoucher.getUser().getId())) {
            return;
        }

        try {
            String message = String.format(
                "Nhắc nhở: Voucher %s của bạn sẽ hết hạn vào %s. " +
                "Hãy sử dụng voucher trước khi hết hạn để không bỏ lỡ ưu đãi!",
                userVoucher.getVoucher().getCode(),
                formatDateTime(userVoucher.getVoucher().getEndsAt())
            );
            
            log.info("Sending voucher expiration reminder to user {}: {}", 
                    userVoucher.getUser().getId(), message);
            
            sendNotificationToUser(userVoucher.getUser().getId(), "Voucher sắp hết hạn", message);
            
        } catch (Exception e) {
            log.error("Failed to send voucher expiration reminder to user {}: {}", 
                     userVoucher.getUser().getId(), e.getMessage(), e);
        }
    }

    @Override
    public void sendVoucherDeactivationNotification(UserVoucherModel userVoucher, String reason) {
        if (!isVoucherNotificationEnabled(userVoucher.getUser().getId())) {
            return;
        }

        try {
            String message = String.format(
                "Voucher %s của bạn đã bị vô hiệu hóa. " +
                "Lý do: %s. Nếu có thắc mắc, vui lòng liên hệ bộ phận hỗ trợ.",
                userVoucher.getVoucher().getCode(),
                reason != null ? reason : "Không có lý do cụ thể"
            );
            
            log.info("Sending voucher deactivation notification to user {}: {}", 
                    userVoucher.getUser().getId(), message);
            
            sendNotificationToUser(userVoucher.getUser().getId(), "Voucher bị vô hiệu hóa", message);
            
        } catch (Exception e) {
            log.error("Failed to send voucher deactivation notification to user {}: {}", 
                     userVoucher.getUser().getId(), e.getMessage(), e);
        }
    }

    @Override
    public boolean isVoucherNotificationEnabled(Long userId) {
        // TODO: Implement user notification preferences check
        // For now, assume all users have notifications enabled
        // In a real implementation, this would check user preferences from database
        return true;
    }

    // Helper methods

    private void sendNotificationToUser(Long userId, String title, String message) {
        // TODO: Implement actual notification delivery
        // This could integrate with:
        // - Email service (SendGrid, AWS SES, etc.)
        // - Push notification service (Firebase, OneSignal, etc.)
        // - In-app notification system
        // - SMS service
        
        log.info("NOTIFICATION [User: {}] [Title: {}] [Message: {}]", userId, title, message);
        
        // Simulate notification sending delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String formatVoucherValue(base.api.entity.VoucherModel voucher) {
        if (voucher.getType() == base.api.enums.VoucherType.PERCENTAGE) {
            return voucher.getPercent() + "% giảm giá";
        } else {
            return String.format("%.0f VNĐ giảm giá", voucher.getAmount());
        }
    }

    private String formatDateTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) {
            return "không giới hạn";
        }
        
        java.time.format.DateTimeFormatter formatter = 
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateTime.format(formatter);
    }
}