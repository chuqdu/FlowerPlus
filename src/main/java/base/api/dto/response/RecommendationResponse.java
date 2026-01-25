package base.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {
    private Integer count;
    private List<RecommendationItem> recommendations;
    private Boolean success;
    private Integer userId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationItem {
        private Integer favorite_count;
        private String images;
        private String name;
        private Integer price;
        private Integer product_id;
        private String purchase_count;
        private String reason;
        private Double score;
        private Integer stock;
    }
}
