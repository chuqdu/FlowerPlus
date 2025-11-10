package base.api.dto.request;

import lombok.Data;

@Data
public class CartDto {
    private long userId;
    private long productId;
    private int quantity;
    private boolean isAdd;
}
