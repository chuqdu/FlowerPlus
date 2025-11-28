package base.api.dto.response;

import base.api.enums.VoucherType;
import lombok.Data;

import java.util.Set;

@Data
public class ValidateVoucherResponse {
    private boolean valid;
    private String message;

    private String code;
    private VoucherType type;

    private Double discountAmount;
    private Double applicableSubtotal;
    private Double finalPayable;

    private Boolean applyAllProducts;
    private Set<Long> appliedProductIds;
}

