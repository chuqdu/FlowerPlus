package base.api.service.impl;

import base.api.entity.UserVoucherModel;
import base.api.repository.IUserVoucherRepository;
import base.api.service.INotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class VoucherReminderScheduler {

    @Autowired
    private IUserVoucherRepository userVoucherRepo;
    
    @Autowired
    private INotificationService notificationService;

    /**
     * Send expiration reminders every day at 9:00 AM
     * This will find vouchers expiring in the next 24 hours and send reminders
     */
    @Scheduled(cron = "0 0 9 * * *") // Every day at 9:00 AM
    public void sendExpirationReminders() {
        log.info("Starting voucher expiration reminder job");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime tomorrow = now.plusDays(1);
            
            List<UserVoucherModel> expiringSoon = userVoucherRepo.findVouchersExpiringSoon(now, tomorrow);
            
            log.info("Found {} vouchers expiring in the next 24 hours", expiringSoon.size());
            
            int successCount = 0;
            int failureCount = 0;
            
            for (UserVoucherModel userVoucher : expiringSoon) {
                try {
                    notificationService.sendVoucherExpirationReminder(userVoucher);
                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to send expiration reminder for voucher {} to user {}: {}", 
                             userVoucher.getVoucher().getCode(), 
                             userVoucher.getUser().getId(), 
                             e.getMessage());
                    failureCount++;
                }
            }
            
            log.info("Voucher expiration reminder job completed. Success: {}, Failures: {}", 
                    successCount, failureCount);
            
        } catch (Exception e) {
            log.error("Error in voucher expiration reminder job: {}", e.getMessage(), e);
        }
    }

    /**
     * Send expiration reminders every hour during business hours (8 AM - 8 PM)
     * This is for vouchers expiring within the next 2 hours (more urgent)
     */
    @Scheduled(cron = "0 0 8-20 * * *") // Every hour from 8 AM to 8 PM
    public void sendUrgentExpirationReminders() {
        log.info("Starting urgent voucher expiration reminder job");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime twoHoursLater = now.plusHours(2);
            
            List<UserVoucherModel> expiringSoon = userVoucherRepo.findVouchersExpiringSoon(now, twoHoursLater);
            
            log.info("Found {} vouchers expiring in the next 2 hours", expiringSoon.size());
            
            for (UserVoucherModel userVoucher : expiringSoon) {
                try {
                    // Send urgent reminder with different message
                    sendUrgentExpirationReminder(userVoucher);
                } catch (Exception e) {
                    log.error("Failed to send urgent expiration reminder for voucher {} to user {}: {}", 
                             userVoucher.getVoucher().getCode(), 
                             userVoucher.getUser().getId(), 
                             e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("Error in urgent voucher expiration reminder job: {}", e.getMessage(), e);
        }
    }

    private void sendUrgentExpirationReminder(UserVoucherModel userVoucher) {
        if (!notificationService.isVoucherNotificationEnabled(userVoucher.getUser().getId())) {
            return;
        }

        try {
            // Calculate remaining time
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiryTime = userVoucher.getVoucher().getEndsAt();
            long hoursRemaining = java.time.Duration.between(now, expiryTime).toHours();
            
            String message = String.format(
                "⚠️ KHẨN CẤP: Voucher %s của bạn sẽ hết hạn trong %d giờ nữa! " +
                "Hãy sử dụng ngay để không bỏ lỡ ưu đãi.",
                userVoucher.getVoucher().getCode(),
                hoursRemaining
            );
            
            log.info("Sending urgent voucher expiration reminder to user {}: {}", 
                    userVoucher.getUser().getId(), message);
            
            // Use a different notification method for urgent reminders (e.g., push notification)
            sendUrgentNotificationToUser(userVoucher.getUser().getId(), "Voucher sắp hết hạn!", message);
            
        } catch (Exception e) {
            log.error("Failed to send urgent voucher expiration reminder to user {}: {}", 
                     userVoucher.getUser().getId(), e.getMessage(), e);
        }
    }

    private void sendUrgentNotificationToUser(Long userId, String title, String message) {
        // TODO: Implement urgent notification delivery (e.g., push notification)
        log.warn("URGENT NOTIFICATION [User: {}] [Title: {}] [Message: {}]", userId, title, message);
    }
}