package base.api.service;

import base.api.dto.request.SyncCategoryRequest;
import base.api.dto.request.SyncProductRequest;

public interface ISyncService {
    void syncCategoryBatch();
    void syncProductBatch();
    boolean syncCategory(SyncCategoryRequest request);
    boolean syncProduct(SyncProductRequest request);
    boolean syncProductUpdate(SyncProductRequest request);
    void syncProductUpdateAsync(SyncProductRequest request);
    String generateProductString(Long productId);
    base.api.dto.response.SyncStatsResponse getSyncStats();
}