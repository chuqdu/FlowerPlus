package base.api.service;

import base.api.entity.UserVoucherModel;

public interface INotificationService {
    
    /**
     * Send notification when a personal voucher is assigned to a user
     */
    void sendVoucherAssignmentNotification(UserVoucherModel userVoucher);
    
    /**
     * Send notification when a personal voucher is used successfully
     */
    void sendVoucherUsageConfirmation(UserVoucherModel userVoucher);
    
    /**
     * Send reminder notification when a voucher is about to expire
     */
    void sendVoucherExpirationReminder(UserVoucherModel userVoucher);
    
    /**
     * Send notification when a personal voucher is deactivated by admin
     */
    void sendVoucherDeactivationNotification(UserVoucherModel userVoucher, String reason);
    
    /**
     * Check if user has notification preferences enabled for voucher notifications
     */
    boolean isVoucherNotificationEnabled(Long userId);
}