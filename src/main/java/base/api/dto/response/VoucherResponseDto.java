package base.api.dto.response;

import base.api.enums.VoucherType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class VoucherResponseDto {
    private Long id;
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
}

