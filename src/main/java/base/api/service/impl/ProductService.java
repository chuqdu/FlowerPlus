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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService implements IProductService {

    @Autowired
    private ICategoryRepository categoryRepository;

    @Autowired
    private IProductRepository productRepository;

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

                // Union categories của các child
                Set<CategoryModel> unionCategories = new LinkedHashSet<>();

                for (ProductCompositionDto compDto : dto.getCompositions()) {
                    if (compDto == null || compDto.getChildProductId() == null) {
                        throw new IllegalArgumentException("Child product id is required");
                    }
                    ProductModel child = productRepository.findById(compDto.getChildProductId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Child product not found: " + compDto.getChildProductId()));

                    // Không cho phép product lồng nhau
                    if (child.getProductType() == ProductType.PRODUCT) {
                        throw new IllegalArgumentException("Child product cannot be a PRODUCT (no nested combos)");
                    }

                    // Gom category của child
                    if (child.getProductCategories() != null) {
                        for (ProductCategoryModel pcm : child.getProductCategories()) {
                            if (pcm.getCategory() != null) unionCategories.add(pcm.getCategory());
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

                // Gán category cho combo = union của các child
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

        return productRepository.save(product);
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
                Set<CategoryModel> unionCategories = new LinkedHashSet<>();
                
                for (ProductCompositionDto compDto : dto.getCompositions()) {
                    if (compDto == null || compDto.getChildProductId() == null) {
                        throw new IllegalArgumentException("Child product id is required");
                    }
                    
                    ProductModel child = productRepository.findById(compDto.getChildProductId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Child product not found: " + compDto.getChildProductId()));

                    if (child.getProductType() == ProductType.PRODUCT) {
                        throw new IllegalArgumentException("Child product cannot be a PRODUCT");
                    }

                    if (child.getProductCategories() != null) {
                        for (ProductCategoryModel pcm : child.getProductCategories()) {
                            if (pcm.getCategory() != null) {
                                unionCategories.add(pcm.getCategory());
                            }
                        }
                    }

                    ProductCompositionModel comp = new ProductCompositionModel();
                    comp.setParent(existingProduct);
                    comp.setChild(child);
                    comp.setQuantity(compDto.getQuantity() == null || compDto.getQuantity() <= 0 ? 1 : compDto.getQuantity());
                    existingProduct.getCompositions().add(comp);
                }

                for (CategoryModel cat : unionCategories) {
                    ProductCategoryModel pcm = new ProductCategoryModel();
                    pcm.setProduct(existingProduct);
                    pcm.setCategory(cat);
                    existingProduct.getProductCategories().add(pcm);
                }
            }
        }

        return productRepository.save(existingProduct);
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
        Set<CategoryModel> unionCategories = new LinkedHashSet<>();

        for (ProductCompositionDto compDto : dto.getCompositions()) {
            if (compDto.getChildProductId() == null) {
                throw new IllegalArgumentException("Child product id is required");
            }

            ProductModel child = productRepository.findById(compDto.getChildProductId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Child product not found: " + compDto.getChildProductId()));

            if (child.getProductType() == ProductType.PRODUCT) {
                throw new IllegalArgumentException("Child product cannot be a PRODUCT (no nested combos)");
            }

            if (child.getProductCategories() != null) {
                for (ProductCategoryModel pcm : child.getProductCategories()) {
                    if (pcm.getCategory() != null) unionCategories.add(pcm.getCategory());
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
                var pcJoin = root.join("productCategories", jakarta.persistence.criteria.JoinType.INNER);
                var catJoin = pcJoin.join("category", jakarta.persistence.criteria.JoinType.INNER);
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
        r.setImages(m.getImages());

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

        if (m.getProductType() == ProductType.PRODUCT && m.getCompositions() != null) {
            r.setCompositions(
                    m.getCompositions().stream()
                            .map(this::toCompositionItem)
                            .collect(Collectors.toList())
            );
        }

        return r;
    }

    private ProductResponse.CompositionItem toCompositionItem(ProductCompositionModel comp) {
        ProductResponse.CompositionItem item = new ProductResponse.CompositionItem();
        if (comp.getChild() != null) {
            item.setChildId(comp.getChild().getId());
            item.setChildName(comp.getChild().getName());
            item.setChildType(comp.getChild().getProductType());
            item.setChildPrice(comp.getChild().getPrice());
            item.setChildImage(comp.getChild().getImages());
        }
        item.setQuantity(
                comp.getQuantity() == null || comp.getQuantity() <= 0 ? 1 : comp.getQuantity()
        );
        return item;
    }
}
