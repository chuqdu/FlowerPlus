package base.api.service;

import base.api.dto.request.SyncCategoryRequest;
import base.api.dto.request.SyncProductRequest;
import base.api.entity.ProductModel;

public interface ISyncService {
    void syncCategoryBatch();
    void syncProductBatch();
    boolean syncCategory(SyncCategoryRequest request);
    boolean syncProduct(SyncProductRequest request);
    void syncProductAfterSave(ProductModel product, boolean isNew);
    String generateProductString(Long productId);
    base.api.dto.response.SyncStatsResponse getSyncStats();
}