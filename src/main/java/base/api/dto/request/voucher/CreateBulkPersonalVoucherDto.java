package base.api.dto.request.voucher;

import base.api.enums.VoucherType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class CreateBulkPersonalVoucherDto {
    // Voucher template properties
    private VoucherType type; // PERCENTAGE or FIXED
    private Double percent; // required if type = PERCENTAGE
    private Double amount;  // required if type = FIXED
    private Double minOrderValue;
    private Double maxDiscountAmount;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private Integer usageLimit;
    private Boolean applyAllProducts = true;
    private Set<Long> productIds;
    
    // Bulk creation specific properties
    private List<Long> targetUserIds; // List of users who will receive vouchers
    private String codePrefix; // Optional prefix for auto-generated codes
    private String description; // Optional description for all voucher assignments
}