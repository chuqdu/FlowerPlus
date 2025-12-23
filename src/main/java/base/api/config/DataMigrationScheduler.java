package base.api.config;

import base.api.entity.CategoryModel;
import base.api.entity.ProductModel;
import base.api.enums.SyncStatus;
import base.api.repository.ICategoryRepository;
import base.api.repository.IProductRepository;
import base.api.service.ISyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "sync.migration.enabled", havingValue = "true", matchIfMissing = true)
public class DataMigrationScheduler {

    private final ICategoryRepository categoryRepository;
    private final IProductRepository productRepository;
    private final ISyncService syncService;

    // Chạy migration mỗi 30 giây để fix NULL records
//    @Scheduled(fixedRate = 30000) // 30 seconds
//    @Transactional
//    public void migrateNullSyncStatus() {
//        log.info("Starting NULL sync_status migration...");
//
//        try {
//            // Fix categories with NULL sync_status
//            List<CategoryModel> nullCategories = categoryRepository.findBySyncStatusIsNull();
//            for (CategoryModel category : nullCategories) {
//                category.setSyncStatus(SyncStatus.PENDING);
//                categoryRepository.save(category);
//                log.info("Updated category {} sync_status to PENDING", category.getId());
//            }
//
//            // Fix products with NULL sync_status or product_string
//            List<ProductModel> nullProducts = productRepository.findBySyncStatusIsNullOrProductStringIsNull();
//            for (ProductModel product : nullProducts) {
//                if (product.getSyncStatus() == null) {
//                    product.setSyncStatus(SyncStatus.PENDING);
//                }
//
//                if (product.getProductString() == null || product.getProductString().isEmpty()) {
//                    String productString = syncService.generateProductString(product.getId());
//                    product.setProductString(productString);
//                }
//
//                productRepository.save(product);
//                log.info("Updated product {} sync_status and product_string", product.getId());
//            }
//
//            if (!nullCategories.isEmpty() || !nullProducts.isEmpty()) {
//                log.info("Migration completed. Updated {} categories and {} products",
//                    nullCategories.size(), nullProducts.size());
//            }
//
//        } catch (Exception e) {
//            log.error("Error in NULL sync_status migration: {}", e.getMessage());
//        }
//    }
}