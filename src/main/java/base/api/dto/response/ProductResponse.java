package base.api.dto.response;

import base.api.enums.ProductType;
import base.api.enums.SyncStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private double price;
    private Integer stock;
    private ProductType productType;
    private Boolean isActive;
    private String images;
    private SyncStatus syncStatus;
    private String productString;

    private List<CategoryLite> categories;

    private List<CompositionItem> compositions;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class CategoryLite {
        private Long id;
        private String name;
    }

    @Data
    public static class CompositionItem {
        private Long childId;
        private String childName;
        private ProductType childType;
        private Integer quantity;
        private double childPrice;
        private String childImage;
    }
}
