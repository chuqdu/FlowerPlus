package base.api.dto.request;

import lombok.Data;

@Data
public class AiCreateOrderDto {
    private Long productId;
    private Long userId;
    private Integer quantity = 1; // Mặc định là 1 nếu không có
}

