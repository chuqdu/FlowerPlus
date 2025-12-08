package base.api.dto.request;

import lombok.Data;

@Data
public class CancelOrderDto {
    private String reason; // Lý do hủy
}
