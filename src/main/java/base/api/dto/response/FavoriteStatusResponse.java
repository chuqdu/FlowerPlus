package base.api.dto.response;

import lombok.Data;

@Data
public class FavoriteStatusResponse {
    private Long productId;
    private boolean isFavorited;
    
    public FavoriteStatusResponse(Long productId, boolean isFavorited) {
        this.productId = productId;
        this.isFavorited = isFavorited;
    }
}