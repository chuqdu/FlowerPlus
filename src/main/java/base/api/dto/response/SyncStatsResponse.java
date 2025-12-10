package base.api.dto.response;

import lombok.Data;

@Data
public class SyncStatsResponse {
    private CategoryStats categories;
    private ProductStats products;

    @Data
    public static class CategoryStats {
        private long total;
        private long pending;
        private long syncing;
        private long synced;
        private long failed;
    }

    @Data
    public static class ProductStats {
        private long total;
        private long pending;
        private long syncing;
        private long synced;
        private long failed;
    }
}