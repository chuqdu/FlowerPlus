package base.api.dto.response;

import base.api.enums.VoucherType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVoucherListDto {
    // UserVoucher info
    private Long userVoucherId;
    private Long voucherId;
    private LocalDateTime assignedAt;
    private Boolean isUsed;
    private LocalDateTime usedAt;
    
    // Voucher details (simplified for user view)
    private String code;
    private VoucherType type;
    private Double percent;
    private Double amount;
    private Double minOrderValue;
    private Double maxDiscountAmount;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private Integer usageLimit;
    private Boolean applyAllProducts;
    
    // Status info
    private Boolean isExpired;
    private Boolean isActive;
    private String status; // "ACTIVE", "USED", "EXPIRED", "NOT_STARTED"
    private Long daysUntilExpiry; // null if no expiry date
}