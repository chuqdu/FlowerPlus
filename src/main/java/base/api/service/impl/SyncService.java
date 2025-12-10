package base.api.service.impl;

import base.api.dto.request.SyncCategoryRequest;
import base.api.dto.request.SyncProductRequest;
import base.api.entity.CategoryModel;
import base.api.entity.ProductModel;
import base.api.enums.SyncStatus;
import base.api.repository.ICategoryRepository;
import base.api.repository.IProductRepository;
import base.api.service.ISyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncService implements ISyncService {

    private final ICategoryRepository categoryRepository;
    private final IProductRepository productRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${sync.category.url:https://good-fun.org/category}")
    private String categoryUrl;

    @Value("${sync.product.url:https://good-fun.org/product}")
    private String productUrl;

    @Value("${sync.batch.size:10}")
    private int batchSize;

    @Override
    @Async
    @Transactional
    public void syncCategoryBatch() {
        log.info("Starting category batch sync...");
        
        List<CategoryModel> pendingCategories = categoryRepository.findBySyncStatusOrderByUpdatedAtAsc(
            SyncStatus.PENDING, 
            org.springframework.data.domain.PageRequest.of(0, batchSize)
        );

        for (CategoryModel category : pendingCategories) {
            try {
                // Update status to SYNCING
                category.setSyncStatus(SyncStatus.SYNCING);
                categoryRepository.save(category);

                // Prepare sync request
                SyncCategoryRequest request = new SyncCategoryRequest();
                request.setCategory_id(category.getId());
                request.setCategory_name(category.getName());

                // Sync
                boolean success = syncCategory(request);
                
                // Update status based on result
                category.setSyncStatus(success ? SyncStatus.SYNCED : SyncStatus.FAILED);
                categoryRepository.save(category);

                log.info("Category {} sync {}", category.getId(), success ? "successful" : "failed");
                
            } catch (Exception e) {
                log.error("Error syncing category {}: {}", category.getId(), e.getMessage());
                category.setSyncStatus(SyncStatus.FAILED);
                categoryRepository.save(category);
            }
        }
        
        log.info("Category batch sync completed. Processed {} categories", pendingCategories.size());
    }

    @Override
    @Async
    @Transactional
    public void syncProductBatch() {
        log.info("Starting product batch sync...");
        
        List<ProductModel> pendingProducts = productRepository.findBySyncStatusOrderByUpdatedAtAsc(
            SyncStatus.PENDING, 
            org.springframework.data.domain.PageRequest.of(0, batchSize)
        );

        for (ProductModel product : pendingProducts) {
            try {
                // Update status to SYNCING
                product.setSyncStatus(SyncStatus.SYNCING);
                productRepository.save(product);

                // Generate product string if not exists
                if (product.getProductString() == null || product.getProductString().isEmpty()) {
                    String productString = generateProductString(product.getId());
                    product.setProductString(productString);
                    productRepository.save(product);
                }

                // Get primary category
                Long categoryId = product.getProductCategories().isEmpty() ? 
                    null : product.getProductCategories().get(0).getCategory().getId();

                // Prepare sync request
                SyncProductRequest request = new SyncProductRequest();
                request.setProduct_id(product.getId());
                request.setProduct_name(product.getName());
                request.setPrice(product.getPrice());
                request.setCategory_id(categoryId);
                request.setProduct_string(product.getProductString());

                // Sync
                boolean success = syncProduct(request);
                
                // Update status based on result
                product.setSyncStatus(success ? SyncStatus.SYNCED : SyncStatus.FAILED);
                productRepository.save(product);

                log.info("Product {} sync {}", product.getId(), success ? "successful" : "failed");
                
            } catch (Exception e) {
                log.error("Error syncing product {}: {}", product.getId(), e.getMessage());
                product.setSyncStatus(SyncStatus.FAILED);
                productRepository.save(product);
            }
        }
        
        log.info("Product batch sync completed. Processed {} products", pendingProducts.size());
    }

    @Override
    public boolean syncCategory(SyncCategoryRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<SyncCategoryRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(categoryUrl, entity, String.class);
            
            return response.getStatusCode().is2xxSuccessful();
            
        } catch (Exception e) {
            log.error("Error calling category sync API: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean syncProduct(SyncProductRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<SyncProductRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(productUrl, entity, String.class);
            
            return response.getStatusCode().is2xxSuccessful();
            
        } catch (Exception e) {
            log.error("Error calling product sync API: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String generateProductString(Long productId) {
        ProductModel product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return "";
        }

        StringBuilder productString = new StringBuilder();
        
        // Basic info
        productString.append("Tên sản phẩm: ").append(product.getName()).append(". ");
        productString.append("Mô tả: ").append(product.getDescription() != null ? product.getDescription() : "Không có mô tả").append(". ");
        productString.append("Giá: ").append(product.getPrice()).append(" VND. ");
        productString.append("Số lượng tồn kho: ").append(product.getStock() != null ? product.getStock() : 0).append(". ");
        productString.append("Loại sản phẩm: ").append(product.getProductType() != null ? product.getProductType().toString() : "Không xác định").append(". ");
        
        // Categories
        if (!product.getProductCategories().isEmpty()) {
            productString.append("Danh mục: ");
            product.getProductCategories().forEach(pc -> 
                productString.append(pc.getCategory().getName()).append(", ")
            );
            productString.setLength(productString.length() - 2); // Remove last comma
            productString.append(". ");
        }
        
        // Images
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            productString.append("Có hình ảnh minh họa. ");
        }
        
        // Timestamps
        if (product.getCreatedAt() != null) {
            productString.append("Ngày tạo: ").append(
                product.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            ).append(". ");
        }
        
        if (product.getUpdatedAt() != null) {
            productString.append("Ngày cập nhật: ").append(
                product.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            ).append(". ");
        }
        
        // Status
        productString.append("Trạng thái: ").append(product.getIsActive() ? "Đang bán" : "Ngừng bán").append(". ");
        
        if (product.isCustom()) {
            productString.append("Sản phẩm tùy chỉnh. ");
        }

        return productString.toString();
    }
}