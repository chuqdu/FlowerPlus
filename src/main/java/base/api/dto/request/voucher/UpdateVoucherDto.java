package base.api.dto.request.voucher;

import base.api.enums.VoucherType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UpdateVoucherDto {
    private VoucherType type; // PERCENTAGE or FIXED
    private Double percent;
    private Double amount;
    private Double minOrderValue;
    private Double maxDiscountAmount;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private Integer usageLimit;
    private Boolean applyAllProducts;
    private Set<Long> productIds;
}

