package base.api.dto.request.voucher;

import lombok.Data;

@Data
public class ValidateVoucherRequestItem {
    private Long productId;
    private double unitPrice;
    private int quantity;

    public double getLineTotal() {
        return unitPrice * quantity;
    }
}

