package base.api.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_vouchers", indexes = {
    @Index(name = "idx_user_voucher_user_id", columnList = "user_id"),
    @Index(name = "idx_user_voucher_voucher_id", columnList = "voucher_id"),
    @Index(name = "idx_user_voucher_assigned_at", columnList = "assigned_at"),
    @Index(name = "idx_user_voucher_is_used", columnList = "is_used")
})
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserVoucherModel extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private VoucherModel voucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UserModel user;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy; // Admin username who created this assignment

    // Helper methods
    public boolean isExpired() {
        if (voucher == null || voucher.getEndsAt() == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(voucher.getEndsAt());
    }

    public boolean isActive() {
        if (voucher == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        
        // Check if voucher is within time window
        if (voucher.getStartsAt() != null && now.isBefore(voucher.getStartsAt())) {
            return false;
        }
        if (voucher.getEndsAt() != null && now.isAfter(voucher.getEndsAt())) {
            return false;
        }
        
        // Check if not used and within usage limit
        if (isUsed) {
            return false;
        }
        
        if (voucher.getUsageLimit() != null && voucher.getUsedCount() != null) {
            return voucher.getUsedCount() < voucher.getUsageLimit();
        }
        
        return true;
    }

    public void markAsUsed() {
        this.isUsed = true;
        this.usedAt = LocalDateTime.now();
    }
}