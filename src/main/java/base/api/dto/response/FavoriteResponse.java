package base.api.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FavoriteResponse {
    private Long id;
    private Long userId;
    private Long productId;
    private ProductResponse product;
    private LocalDateTime createdAt;
}