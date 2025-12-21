package base.api.dto.request;

import lombok.Data;

@Data
public class FavoriteDto {
    private Long productId;
    private Long userId;
}