package base.api.dto.response;

import base.api.enums.VoucherType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class PersonalVoucherResponseDto {
    // UserVoucher assignment info
    private Long userVoucherId;
    private Long userId;
    private String userName;
    private String userEmail;
    private LocalDateTime assignedAt;
    private Boolean isUsed;
    private LocalDateTime usedAt;
    private String createdBy;
    
    // Voucher details
    private Long voucherId;
    private String code;
    private VoucherType type;
    private Double percent;
    private Double amount;
    private Double minOrderValue;
    private Double maxDiscountAmount;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private Integer usageLimit;
    private Integer usedCount;
    private Boolean applyAllProducts;
    private Set<Long> productIds;
    
    // Computed fields
    private Boolean isExpired;
    private Boolean isActive;
    private Integer remainingUsage;
}