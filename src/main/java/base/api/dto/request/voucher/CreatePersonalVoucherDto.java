package base.api.dto.request.voucher;

import base.api.enums.VoucherType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class CreatePersonalVoucherDto {
    // Voucher properties (inherited from CreateVoucherDto)
    private String code; // Optional - will auto-generate if not provided
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
    
    // Personal voucher specific properties
    private Long targetUserId; // The user who will receive this voucher
    private String description; // Optional description for the voucher assignment
}