package base.api.service.impl;

import base.api.dto.request.ProductCompositionDto;
import base.api.dto.request.ProductDto;
import base.api.dto.request.paging.PageableRequestDTO;
import base.api.dto.response.ProductResponse;
import base.api.entity.CategoryModel;
import base.api.entity.ProductCategoryModel;
import base.api.entity.ProductCompositionModel;
import base.api.entity.ProductModel;
import base.api.enums.ProductType;
import base.api.enums.SyncStatus;
import base.api.repository.ICategoryRepository;
import base.api.repository.IProductRepository;
import base.api.service.IProductService;
import base.api.service.ISyncService;
import jakarta.persistence.criteria.JoinType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductService implements IProductService {

    @Autowired
    private ICategoryRepository categoryRepository;

    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private ISyncService syncService;

    @Transactional
    @Override
    public ProductModel createProduct(ProductDto dto) {
        if (dto == null) throw new IllegalArgumentException("ProductDto is required");
        if (dto.getName() == null || dto.getName().isBlank())
            throw new IllegalArgumentException("Product name is required");
        if (dto.getProductType() == null)
            throw new IllegalArgumentException("Product type is required");
        if (dto.getPrice() < 0)
            throw new IllegalArgumentException("Price must be >= 0");

        // Khởi tạo object
        ProductModel product = new ProductModel();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setUserId(dto.getUserId());
        product.setCustom(dto.isCustom());
        product.setProductType(dto.getProductType());
        product.setIsActive(dto.getIsActive() == null ? Boolean.TRUE : dto.getIsActive());
        product.setImages(dto.getImages());
        product.setSyncStatus(SyncStatus.PENDING); // Set default sync status
        
        // Log product details for debugging
        log.info("Creating product - Name: {}, Price: {}, Stock: {}, Type: {}", 
            product.getName(), product.getPrice(), product.getStock(), product.getProductType());

        switch (dto.getProductType()) {
            case FLOWER:
            case ITEM: {
                if (dto.getCategoryIds() == null || dto.getCategoryIds().isEmpty()) {
                    throw new IllegalArgumentException("FLOWER/ITEM must have at least one categoryId");
                }
                List<Long> ids = dto.getCategoryIds();
                List<CategoryModel> categories = categoryRepository.findAllById(ids);
                if (categories.size() != ids.size()) {
                    throw new IllegalArgumentException("Some categoryIds do not exist");
                }

                product = productRepository.save(product);

                for (CategoryModel cat : categories) {
                    ProductCategoryModel pcm = new ProductCategoryModel();
                    pcm.setProduct(product);
                    pcm.setCategory(cat);
                    product.getProductCategories().add(pcm);
                }

                // Không cho truyền compositions cho FLOWER/ITEM
                if (dto.getCompositions() != null && !dto.getCompositions().isEmpty()) {
                    throw new IllegalArgumentException("FLOWER/ITEM cannot contain compositions");
                }
                break;
            }

            case PRODUCT: {
                // PRODUCT (combo) bắt buộc có compositions
                if (dto.getCompositions() == null || dto.getCompositions().isEmpty()) {
                    throw new IllegalArgumentException("PRODUCT must contain at least one child item");
                }

                // Lưu product để có id cho liên kết
                product = productRepository.save(product);

                // Bước 1: Lấy các sản phẩm con
                // Bước 2: Lấy ra các danh mục của sản phẩm con đó
                // Bước 3: Tập hợp lại để làm danh mục cho sản phẩm cha
                Set<CategoryModel> unionCategories = new LinkedHashSet<>();

                for (ProductCompositionDto compDto : dto.getCompositions()) {
                    if (compDto == null || compDto.getChildProductId() == null) {
                        throw new IllegalArgumentException("Child product id is required");
                    }
                    
                    // Bước 1: Lấy sản phẩm con với đầy đủ categories
                    ProductModel child = productRepository.findByIdWithCategories(compDto.getChildProductId())
                            .orElse(productRepository.findById(compDto.getChildProductId())
                                    .orElseThrow(() -> new IllegalArgumentException(
                                            "Child product not found: " + compDto.getChildProductId())));

                    // Không cho phép product lồng nhau
                    if (child.getProductType() == ProductType.PRODUCT) {
                        throw new IllegalArgumentException("Child product cannot be a PRODUCT (no nested combos)");
                    }

                    // Bước 2: Lấy ra các danh mục của sản phẩm con
                    if (child.getProductCategories() != null && !child.getProductCategories().isEmpty()) {
                        for (ProductCategoryModel pcm : child.getProductCategories()) {
                            CategoryModel category = pcm.getCategory();
                            if (category != null) {
                                // Bước 3: Tập hợp lại (Set tự động loại bỏ duplicate)
                                unionCategories.add(category);
                            }
                        }
                    }

                    // Tạo dòng composition
                    ProductCompositionModel comp = new ProductCompositionModel();
                    comp.setParent(product);
                    comp.setChild(child);
                    comp.setQuantity(
                            compDto.getQuantity() == null || compDto.getQuantity() <= 0 ? 1 : compDto.getQuantity()
                    );
                    product.getCompositions().add(comp);
                }

                // Bước 3: Gán tất cả categories đã tập hợp cho sản phẩm cha
                for (CategoryModel cat : unionCategories) {
                    ProductCategoryModel pcm = new ProductCategoryModel();
                    pcm.setProduct(product);
                    pcm.setCategory(cat);
                    product.getProductCategories().add(pcm);
                }

                // Bỏ qua dto.categoryIds
                break;
            }

            default:
                throw new IllegalStateException("Unsupported product type: " + dto.getProductType());
        }

        ProductModel savedProduct = productRepository.save(product);
        
        // Tự động sync với AI sau khi tạo thành công (chạy ngầm, không ảnh hưởng luồng chính)
        syncService.syncProductAfterSave(savedProduct, true);
        
        return savedProduct;
    }

    @Transactional
    @Override
    public ProductModel updateProduct(ProductDto dto) {
        if (dto == null || dto.getId() == null) {
            throw new IllegalArgumentException("ProductDto and ID are required");
        }

        ProductModel existingProduct = productRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + dto.getId()));

        existingProduct.setName(dto.getName());
        existingProduct.setDescription(dto.getDescription());
        existingProduct.setPrice(dto.getPrice());
        existingProduct.setStock(dto.getStock());
        if (dto.getIsActive() != null) {
            existingProduct.setIsActive(dto.getIsActive());
        }
        if (dto.getImages() != null) {
            existingProduct.setImages(dto.getImages());
        }
        
        // Reset sync status when updating
        existingProduct.setSyncStatus(SyncStatus.PENDING);

        if (existingProduct.getProductType() == ProductType.FLOWER || 
            existingProduct.getProductType() == ProductType.ITEM) {
            existingProduct.getProductCategories().clear();
            
            if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
                List<CategoryModel> categories = categoryRepository.findAllById(dto.getCategoryIds());
                for (CategoryModel cat : categories) {
                    ProductCategoryModel pcm = new ProductCategoryModel();
                    pcm.setProduct(existingProduct);
                    pcm.setCategory(cat);
                    existingProduct.getProductCategories().add(pcm);
                }
            }
        }

        if (existingProduct.getProductType() == ProductType.PRODUCT) {
            existingProduct.getCompositions().clear();
            existingProduct.getProductCategories().clear();
            
            if (dto.getCompositions() != null && !dto.getCompositions().isEmpty()) {
                // Bước 1: Lấy các sản phẩm con
                // Bước 2: Lấy ra các danh mục của sản phẩm con đó
                // Bước 3: Tập hợp lại để làm danh mục cho sản phẩm cha
                Set<CategoryModel> unionCategories = new LinkedHashSet<>();
                
                for (ProductCompositionDto compDto : dto.getCompositions()) {
                    if (compDto == null || compDto.getChildProductId() == null) {
                        throw new IllegalArgumentException("Child product id is required");
                    }
                    
                    // Bước 1: Lấy sản phẩm con với đầy đủ categories
                    ProductModel child = productRepository.findByIdWithCategories(compDto.getChildProductId())
                            .orElse(productRepository.findById(compDto.getChildProductId())
                                    .orElseThrow(() -> new IllegalArgumentException(
                                            "Child product not found: " + compDto.getChildProductId())));

                    if (child.getProductType() == ProductType.PRODUCT) {
                        throw new IllegalArgumentException("Child product cannot be a PRODUCT");
                    }

                    // Bước 2: Lấy ra các danh mục của sản phẩm con
                    if (child.getProductCategories() != null && !child.getProductCategories().isEmpty()) {
                        for (ProductCategoryModel pcm : child.getProductCategories()) {
                            CategoryModel category = pcm.getCategory();
                            if (category != null) {
                                // Bước 3: Tập hợp lại (Set tự động loại bỏ duplicate)
                                unionCategories.add(category);
                            }
                        }
                    }

                    // Tạo dòng composition
                    ProductCompositionModel comp = new ProductCompositionModel();
                    comp.setParent(existingProduct);
                    comp.setChild(child);
                    comp.setQuantity(compDto.getQuantity() == null || compDto.getQuantity() <= 0 ? 1 : compDto.getQuantity());
                    existingProduct.getCompositions().add(comp);
                }

                // Bước 3: Gán tất cả categories đã tập hợp cho sản phẩm cha
                for (CategoryModel cat : unionCategories) {
                    ProductCategoryModel pcm = new ProductCategoryModel();
                    pcm.setProduct(existingProduct);
                    pcm.setCategory(cat);
                    existingProduct.getProductCategories().add(pcm);
                }
            }
        }

        ProductModel savedProduct = productRepository.save(existingProduct);
        
        // Tự động sync với AI sau khi update thành công (chạy ngầm, không ảnh hưởng luồng chính)
        syncService.syncProductAfterSave(savedProduct, false);
        
        return savedProduct;
    }

    @Override
    public List<ProductModel> getListProduct() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public ProductResponse getProductById(Long id) {
        ProductModel model = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        return toResponse(model);
    }

    @Override
    public String deleteProduct(Long id) {
        ProductModel existingProduct = productRepository.findById(id).get();

        if(existingProduct != null){
            productRepository.deleteById(id);
        }
        return "Xoa thanh cong";
    }

    private void handleComboProduct(ProductModel product, ProductDto dto) {
        if (dto.getCompositions() == null || dto.getCompositions().isEmpty()) {
            throw new IllegalArgumentException("PRODUCT must contain at least one child item");
        }

        product = productRepository.save(product);
        
        // Bước 1: Lấy các sản phẩm con
        // Bước 2: Lấy ra các danh mục của sản phẩm con đó
        // Bước 3: Tập hợp lại để làm danh mục cho sản phẩm cha
        Set<CategoryModel> unionCategories = new LinkedHashSet<>();

        for (ProductCompositionDto compDto : dto.getCompositions()) {
            if (compDto.getChildProductId() == null) {
                throw new IllegalArgumentException("Child product id is required");
            }

            // Bước 1: Lấy sản phẩm con với đầy đủ categories
            ProductModel child = productRepository.findByIdWithCategories(compDto.getChildProductId())
                    .orElse(productRepository.findById(compDto.getChildProductId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Child product not found: " + compDto.getChildProductId())));

            if (child.getProductType() == ProductType.PRODUCT) {
                throw new IllegalArgumentException("Child product cannot be a PRODUCT (no nested combos)");
            }

            // Bước 2: Lấy ra các danh mục của sản phẩm con
            if (child.getProductCategories() != null && !child.getProductCategories().isEmpty()) {
                for (ProductCategoryModel pcm : child.getProductCategories()) {
                    CategoryModel category = pcm.getCategory();
                    if (category != null) {
                        // Bước 3: Tập hợp lại (Set tự động loại bỏ duplicate)
                        unionCategories.add(category);
                    }
                }
            }

            ProductCompositionModel comp = new ProductCompositionModel();
            comp.setParent(product);
            comp.setChild(child);
            comp.setQuantity(
                    compDto.getQuantity() == null || compDto.getQuantity() <= 0 ? 1 : compDto.getQuantity()
            );
            product.getCompositions().add(comp);
        }

        // Bước 3: Gán tất cả categories đã tập hợp cho sản phẩm cha
        for (CategoryModel cat : unionCategories) {
            ProductCategoryModel pcm = new ProductCategoryModel();
            pcm.setProduct(product);
            pcm.setCategory(cat);
            product.getProductCategories().add(pcm);
        }
    }

    @Transactional
    @Override
    public Page<ProductResponse> getProducts(ProductType type, Boolean active, Long categoryId, Boolean custom, Long userId,
                                             PageableRequestDTO pageableRequest) {
        pageableRequest.validate();
        Pageable pageable = pageableRequest.toPageable();

        Specification<ProductModel> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();

            if (type != null) {
                preds.add(cb.equal(root.get("productType"), type));
            }
            if (active != null) {
                preds.add(cb.equal(root.get("isActive"), active));
            }
            if (custom == true) {
                preds.add(cb.equal(root.get("isCustom"), custom));
                preds.add(cb.equal(root.get("userId"), userId));
            }
            else{
                preds.add(cb.equal(root.get("isCustom"), false));
            }
            if (pageableRequest.getKeyword() != null && !pageableRequest.getKeyword().isBlank()) {
                preds.add(cb.like(cb.lower(root.get("name")),
                        "%" + pageableRequest.getKeyword().toLowerCase() + "%"));
            }

            if (categoryId != null) {
                // Product (parent) -> compositions (ProductCompositionModel)
                var compositionsJoin = root.join("compositions", JoinType.INNER);

                // ProductCompositionModel -> child (ProductModel - thành phần con)
                var childJoin = compositionsJoin.join("child", JoinType.INNER);

                // child ProductModel -> productCategories
                var pcJoin = childJoin.join("productCategories", JoinType.INNER);

                // ProductCategoryModel -> category
                var catJoin = pcJoin.join("category", JoinType.INNER);

                // Filter theo category của thành phần con
                preds.add(cb.equal(catJoin.get("id"), categoryId));

                query.distinct(true);
            }

            return cb.and(preds.toArray(new Predicate[0]));
        };

        Page<ProductModel> page = productRepository.findAll(spec, pageable);
        return page.map(this::toResponse);
    }
    private ProductResponse toResponse(ProductModel m) {
        ProductResponse r = new ProductResponse();
        r.setId(m.getId());
        r.setName(m.getName());
        r.setDescription(m.getDescription());
        r.setPrice(m.getPrice());
        r.setStock(m.getStock());
        r.setProductType(m.getProductType());
        r.setIsActive(m.getIsActive());
        r.setImages(m.getImages() != null ? m.getImages().replace("http://", "https://") : null);
        r.setSyncStatus(m.getSyncStatus());
        r.setProductString(m.getProductString());

        if (m.getProductType() == ProductType.PRODUCT) {
            List<CategoryModel> unionCategories = new ArrayList<>();
            
            if (m.getCompositions() != null && !m.getCompositions().isEmpty()) {
                List<Long> childIds = m.getCompositions().stream()
                        .map(comp -> comp.getChild() != null ? comp.getChild().getId() : null)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());
                
                for (Long childId : childIds) {
                    ProductModel child = productRepository.findByIdWithCategories(childId)
                            .orElse(productRepository.findById(childId).orElse(null));

                    if (child != null && child.getProductCategories() != null) {
                        for (ProductCategoryModel pcm : child.getProductCategories()) {
                            CategoryModel category = pcm.getCategory();
                            if (category != null && category.isPublic) {
                                unionCategories.add(category);
                            }
                        }
                    }
                }
            }
            
            r.setCategories(
                    unionCategories.stream()
                            .map(c -> {
                                ProductResponse.CategoryLite dto = new ProductResponse.CategoryLite();
                                dto.setId(c.getId());
                                dto.setName(c.getName());
                                return dto;
                            })
                            .collect(Collectors.toList())
            );
        } else {
            // FLOWER/ITEM: lấy categories từ productCategories
            if (m.getProductCategories() != null) {
                r.setCategories(
                        m.getProductCategories().stream()
                                .map(ProductCategoryModel::getCategory)
                                .filter(Objects::nonNull)
                                .map(c -> {
                                    ProductResponse.CategoryLite dto = new ProductResponse.CategoryLite();
                                    dto.setId(c.getId());
                                    dto.setName(c.getName());
                                    return dto;
                                })
                                .collect(Collectors.toList())
                );
            }
        }

        if (m.getProductType() == ProductType.PRODUCT && m.getCompositions() != null) {
            r.setCompositions(
                    m.getCompositions().stream()
                            .map(this::toCompositionItem)
                            .collect(Collectors.toList())
            );
        }

        r.setCreatedAt(m.getCreatedAt());
        r.setUpdatedAt(m.getUpdatedAt());

        return r;
    }

    private ProductResponse.CompositionItem toCompositionItem(ProductCompositionModel comp) {
        ProductResponse.CompositionItem item = new ProductResponse.CompositionItem();
        if (comp.getChild() != null) {
            item.setChildId(comp.getChild().getId());
            item.setChildName(comp.getChild().getName());
            item.setChildType(comp.getChild().getProductType());
            item.setChildPrice(comp.getChild().getPrice());
            String childImages = comp.getChild().getImages();
            item.setChildImage(childImages != null ? childImages.replace("http://", "https://") : null);
        }
        item.setQuantity(
                comp.getQuantity() == null || comp.getQuantity() <= 0 ? 1 : comp.getQuantity()
        );
        return item;
    }
}
