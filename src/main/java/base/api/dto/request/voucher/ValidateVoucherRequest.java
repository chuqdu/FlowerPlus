package base.api.dto.request.voucher;

import lombok.Data;

import java.util.List;

@Data
public class ValidateVoucherRequest {
    private String code;
    private boolean useCurrentUserCart = false; // nếu true thì bỏ qua items và dùng cart của user hiện tại
    private List<ValidateVoucherRequestItem> items; // dùng khi không lấy từ cart
}

