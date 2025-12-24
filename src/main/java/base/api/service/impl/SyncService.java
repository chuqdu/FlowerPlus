package base.api.service.impl;

import base.api.base.Util;
import base.api.dto.request.SyncCategoryRequest;
import base.api.dto.request.SyncProductRequest;
import base.api.entity.CategoryModel;
import base.api.entity.ProductModel;
import base.api.enums.ProductType;
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
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncService implements ISyncService {

    private final ICategoryRepository categoryRepository;
    private final IProductRepository productRepository;
    private RestTemplate restTemplate;
    
    @PostConstruct
    public void initRestTemplate() {
        this.restTemplate = new RestTemplate();
        // Configure message converters: Jackson for JSON objects and String for responses
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        
        // Add Jackson converter for JSON serialization
        MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
        // Ensure it supports APPLICATION_JSON
        jacksonConverter.setSupportedMediaTypes(List.of(
            MediaType.APPLICATION_JSON,
            MediaType.APPLICATION_JSON_UTF8
        ));
        messageConverters.add(jacksonConverter);
        
        // Add String converter for text responses
        messageConverters.add(new StringHttpMessageConverter(
            java.nio.charset.StandardCharsets.UTF_8));
        
        this.restTemplate.setMessageConverters(messageConverters);
        
        // Set request factory with timeout
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = 
            new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 10 seconds
        factory.setReadTimeout(30000); // 30 seconds
        this.restTemplate.setRequestFactory(factory);
    }

    @Value("${sync.category.url:https://good-fun.org/category}")
    private String categoryUrl;

    @Value("${sync.product.url:https://good-fun.org/product}")
    private String productUrl;

    @Value("${sync.batch.size:10}")
    private int batchSize;
    
    @Value("${sync.failed.batch.size:30}")
    private int failedBatchSize;

    @Override
    @Async
    @Transactional
    public void syncCategoryBatch() {
        log.info("Starting category batch sync...");
        
        // Get all categories with syncStatus != SYNCED
        List<CategoryModel> pendingCategories = categoryRepository.findCategoriesForSync(
            SyncStatus.SYNCED,
            org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)
        );

        log.info("Found {} categories to sync", pendingCategories.size());

        for (CategoryModel category : pendingCategories) {
            try {
                // Update status to SYNCING
                category.setSyncStatus(SyncStatus.SYNCING);
                categoryRepository.save(category);

                // Prepare sync request
                SyncCategoryRequest request = new SyncCategoryRequest();
                request.setCategory_id(category.getId());
                request.setCategory_name(category.getName());

                // Log payload before sending
                log.info("Syncing category payload: {}", request);

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
        
        // Get all products with type = PRODUCT, isActive = true, price > 0, syncStatus != SYNCED
        List<ProductModel> pendingProducts = productRepository.findProductsForSync(
            SyncStatus.SYNCED,
            ProductType.PRODUCT,
            org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)
        );
        
        log.info("Found {} products to sync", pendingProducts.size());

        for (ProductModel product : pendingProducts) {
            try {
                // Reload product from database to ensure all fields are loaded correctly
                ProductModel freshProduct = productRepository.findById(product.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + product.getId()));
                
                // Log product details for debugging
                log.info("Processing product - ID: {}, Name: {}, Price: {}", 
                    freshProduct.getId(), freshProduct.getName(), freshProduct.getPrice());
                
                // Update status to SYNCING
                freshProduct.setSyncStatus(SyncStatus.SYNCING);
                productRepository.save(freshProduct);

                // Generate product string if not exists
                if (freshProduct.getProductString() == null || freshProduct.getProductString().isEmpty()) {
                    String productString = generateProductString(freshProduct.getId());
                    freshProduct.setProductString(productString);
                    productRepository.save(freshProduct);
                }

                // Get primary category (first category in the list)
                Long categoryId = null;
                if (!freshProduct.getProductCategories().isEmpty()) {
                    categoryId = freshProduct.getProductCategories().get(0).getCategory().getId();
                    log.info("Product {} has {} categories, using primary category: {}", 
                        freshProduct.getId(), freshProduct.getProductCategories().size(), categoryId);
                } else {
                    log.warn("Product {} has no categories assigned, skipping sync", freshProduct.getId());
                    freshProduct.setSyncStatus(SyncStatus.FAILED);
                    productRepository.save(freshProduct);
                    continue;
                }

                // Prepare sync request
                SyncProductRequest request = new SyncProductRequest();
                request.setProduct_id(freshProduct.getId());
                request.setProduct_name(freshProduct.getName());
                request.setPrice(freshProduct.getPrice());
                request.setCategory_id(categoryId);
                request.setProduct_string(freshProduct.getProductString());

                // Log payload before sending
                log.info("Syncing product payload: {}", request);

                // Step 1: Try POST (create)
                boolean success = syncProductCreate(request);
                
                if (!success) {
                    // Step 2: If POST failed, retry with PUT (update)
                    log.warn("Product {} POST failed, trying PUT instead", freshProduct.getId());
                    boolean updateSuccess = syncProductUpdate(request);
                    
                    if (updateSuccess) {
                        log.info("Product {} PUT successful after POST failed", freshProduct.getId());
                        freshProduct.setSyncStatus(SyncStatus.SYNCED);
                    } else {
                        log.error("Product {} both POST and PUT failed", freshProduct.getId());
                        freshProduct.setSyncStatus(SyncStatus.FAILED);
                    }
                } else {
                    // POST successful
                    freshProduct.setSyncStatus(SyncStatus.SYNCED);
                    log.info("Product {} POST successful", freshProduct.getId());
                }
                
                productRepository.save(freshProduct);
                
            } catch (Exception e) {
                log.error("Error syncing product {}: {}", product.getId(), e.getMessage(), e);
                try {
                    ProductModel failedProduct = productRepository.findById(product.getId()).orElse(null);
                    if (failedProduct != null) {
                        failedProduct.setSyncStatus(SyncStatus.FAILED);
                        productRepository.save(failedProduct);
                    }
                } catch (Exception saveException) {
                    log.error("Failed to update sync status for product {}: {}", product.getId(), saveException.getMessage(), saveException);
                }
            }
        }
        
        log.info("Product batch sync completed. Processed {} products", pendingProducts.size());
    }

    @Override
    public boolean syncCategory(SyncCategoryRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            
            // Send object directly - RestTemplate will serialize it to JSON using MappingJackson2HttpMessageConverter
            HttpEntity<SyncCategoryRequest> entity = new HttpEntity<>(request, headers);
            
            // Log the request payload for debugging
            String requestBodyJson = Util.toJson(request);
            log.info("Category sync - URL: {}, Request body: {}", categoryUrl, requestBodyJson);
            
            ResponseEntity<String> response = restTemplate.postForEntity(categoryUrl, entity, String.class);
            
            log.info("Category sync response - Status: {}, Body: {}", response.getStatusCode(), response.getBody());
            return response.getStatusCode().is2xxSuccessful();
            
        } catch (HttpClientErrorException e) {
            log.error("Error calling category sync API: {} - Response body: {}", e.getMessage(), e.getResponseBodyAsString());
            return false;
        } catch (RestClientException e) {
            log.error("Error calling category sync API: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error calling category sync API: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean syncProduct(SyncProductRequest request) {
        // For backward compatibility, use POST with retry PUT
        boolean success = syncProductCreate(request);
        if (!success) {
            success = syncProductUpdate(request);
        }
        return success;
    }

    /**
     * Helper method: POST product (create)
     */
    private boolean syncProductCreate(SyncProductRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.set("User-Agent", "FlowerPlus-SyncService/1.0");
            
            HttpEntity<SyncProductRequest> entity = new HttpEntity<>(request, headers);
            
            String requestBodyJson = Util.toJson(request);
            log.info("Product POST (create) - URL: {}, Request body: {}", productUrl, requestBodyJson);
            
            ResponseEntity<String> response = restTemplate.postForEntity(productUrl, entity, String.class);
            
            log.info("Product POST response - Status: {}, Body: {}", response.getStatusCode(), response.getBody());
            return response.getStatusCode().is2xxSuccessful();
            
        } catch (HttpClientErrorException e) {
            log.error("Error calling product POST API: {} - Status: {} - Response body: {}", 
                e.getMessage(), e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (RestClientException e) {
            log.error("Error calling product POST API: {}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Unexpected error calling product POST API: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Helper method: PUT product (update)
     */
    private boolean syncProductUpdate(SyncProductRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.set("User-Agent", "FlowerPlus-SyncService/1.0");
            
            HttpEntity<SyncProductRequest> entity = new HttpEntity<>(request, headers);
            
            String updateUrl = productUrl + "/" + request.getProduct_id();
            
            String requestBodyJson = Util.toJson(request);
            log.info("Product PUT (update) - URL: {}, Request body: {}", updateUrl, requestBodyJson);
            
            ResponseEntity<Object> response = restTemplate.exchange(
                updateUrl, 
                org.springframework.http.HttpMethod.PUT, 
                entity, 
                Object.class
            );
            
            String responseBody;
            if (response.getBody() == null) {
                responseBody = "null";
            } else if (response.getBody() instanceof String) {
                responseBody = (String) response.getBody();
            } else {
                responseBody = Util.toJson(response.getBody());
            }
            
            log.info("Product PUT response - Status: {}, Body: {}", response.getStatusCode(), responseBody);
            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                log.info("Product {} updated successfully in AI service", request.getProduct_id());
            } else {
                log.warn("Product {} update failed with status: {}", request.getProduct_id(), response.getStatusCode());
            }
            
            return success;
            
        } catch (HttpClientErrorException e) {
            log.error("Error calling product PUT API for product {}: {} - Status: {} - Response body: {}", 
                request.getProduct_id(), e.getMessage(), e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (RestClientException e) {
            log.error("Error calling product PUT API for product {}: {}", request.getProduct_id(), e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Unexpected error calling product PUT API for product {}: {}", request.getProduct_id(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Async
    public void syncProductAfterSave(ProductModel product, boolean isNew) {
        try {
            // Only sync if product is active, has price > 0, and has categories
            if (product.getIsActive() == null || !product.getIsActive()) {
                log.debug("Product {} is not active, skipping sync", product.getId());
                return;
            }
            if (product.getPrice() <= 0) {
                log.debug("Product {} has price <= 0, skipping sync", product.getId());
                return;
            }
            if (product.getProductCategories() == null || product.getProductCategories().isEmpty()) {
                log.debug("Product {} has no categories, skipping sync", product.getId());
                return;
            }
            if (product.getProductType() != ProductType.PRODUCT) {
                log.debug("Product {} is not PRODUCT type, skipping sync", product.getId());
                return;
            }
            
            // Reload product to get fresh data
            ProductModel freshProduct = productRepository.findById(product.getId()).orElse(null);
            if (freshProduct == null) {
                log.warn("Product {} not found for sync", product.getId());
                return;
            }
            
            // Generate product string if not exists
            if (freshProduct.getProductString() == null || freshProduct.getProductString().isEmpty()) {
                String productString = generateProductString(freshProduct.getId());
                freshProduct.setProductString(productString);
                productRepository.save(freshProduct);
            }
            
            // Get primary category
            Long categoryId = null;
            if (!freshProduct.getProductCategories().isEmpty()) {
                categoryId = freshProduct.getProductCategories().get(0).getCategory().getId();
            }
            
            // Prepare sync request
            SyncProductRequest request = new SyncProductRequest();
            request.setProduct_id(freshProduct.getId());
            request.setProduct_name(freshProduct.getName());
            request.setPrice(freshProduct.getPrice());
            request.setCategory_id(categoryId);
            request.setProduct_string(freshProduct.getProductString());
            
            log.info("Auto-syncing product {} (isNew: {})", freshProduct.getId(), isNew);
            
            boolean success = false;
            
            if (isNew) {
                // Create: POST → if fail then retry PUT
                success = syncProductCreate(request);
                if (!success) {
                    log.warn("Product {} POST failed, retrying with PUT", freshProduct.getId());
                    success = syncProductUpdate(request);
                }
            } else {
                // Update: PUT directly
                success = syncProductUpdate(request);
            }
            
            // Update sync status
            freshProduct.setSyncStatus(success ? SyncStatus.SYNCED : SyncStatus.FAILED);
            productRepository.save(freshProduct);
            
            if (success) {
                log.info("Product {} auto-sync successful", freshProduct.getId());
            } else {
                log.warn("Product {} auto-sync failed", freshProduct.getId());
            }
            
        } catch (Exception e) {
            log.error("Error in auto-sync for product {}: {}", product.getId(), e.getMessage(), e);
            try {
                ProductModel failedProduct = productRepository.findById(product.getId()).orElse(null);
                if (failedProduct != null) {
                    failedProduct.setSyncStatus(SyncStatus.FAILED);
                    productRepository.save(failedProduct);
                }
            } catch (Exception saveException) {
                log.error("Failed to update sync status for product {}: {}", product.getId(), saveException.getMessage());
            }
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

    @Override
    public base.api.dto.response.SyncStatsResponse getSyncStats() {
        base.api.dto.response.SyncStatsResponse stats = new base.api.dto.response.SyncStatsResponse();
        
        // Category stats
        base.api.dto.response.SyncStatsResponse.CategoryStats categoryStats = 
            new base.api.dto.response.SyncStatsResponse.CategoryStats();
        categoryStats.setTotal(categoryRepository.count());
        categoryStats.setPending(categoryRepository.countBySyncStatus(SyncStatus.PENDING));
        categoryStats.setSyncing(categoryRepository.countBySyncStatus(SyncStatus.SYNCING));
        categoryStats.setSynced(categoryRepository.countBySyncStatus(SyncStatus.SYNCED));
        categoryStats.setFailed(categoryRepository.countBySyncStatus(SyncStatus.FAILED));
        stats.setCategories(categoryStats);
        
        // Product stats
        base.api.dto.response.SyncStatsResponse.ProductStats productStats = 
            new base.api.dto.response.SyncStatsResponse.ProductStats();
        productStats.setTotal(productRepository.count());
        productStats.setPending(productRepository.countBySyncStatus(SyncStatus.PENDING));
        productStats.setSyncing(productRepository.countBySyncStatus(SyncStatus.SYNCING));
        productStats.setSynced(productRepository.countBySyncStatus(SyncStatus.SYNCED));
        productStats.setFailed(productRepository.countBySyncStatus(SyncStatus.FAILED));
        stats.setProducts(productStats);
        
        return stats;
    }
}