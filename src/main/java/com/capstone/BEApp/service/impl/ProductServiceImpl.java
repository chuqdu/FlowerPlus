package com.capstone.BEApp.service.impl;

import com.capstone.BEApp.dto.category.CategoryDto;
import com.capstone.BEApp.dto.common.ImageDto;
import com.capstone.BEApp.dto.product.*;
import com.capstone.BEApp.entity.*;
import com.capstone.BEApp.entity.enumFile.ProductType;
import com.capstone.BEApp.repository.CategoryRepository;
import com.capstone.BEApp.repository.ProductCompositionRepository;
import com.capstone.BEApp.repository.ProductRepository;
import com.capstone.BEApp.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductCompositionRepository compositionRepository;
    @Override
    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto requestDto) {
        Product product = Product.builder()
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .stock(requestDto.getStock())
                .price(requestDto.getPrice())
                .isPublic(requestDto.getIsPublic() == null ? Boolean.TRUE : requestDto.getIsPublic())
                .isCustom(requestDto.getIsCustom() == null ? Boolean.FALSE : requestDto.getIsCustom())
                .isDisabled(false)
                .type(requestDto.getType() == null ? ProductType.PRODUCT : requestDto.getType())
                .createdAt(LocalDateTime.now())
                .build();

        Product saved = productRepository.save(product);

        if (requestDto.getCategoryIds() != null && !requestDto.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(requestDto.getCategoryIds());
            List<ProductCategory> pcs = categories.stream()
                    .map(cat -> ProductCategory.builder()
                            .product(saved)
                            .category(cat)
                            .build())
                    .collect(Collectors.toList());
            saved.getProductCategories().addAll(pcs);
        }

        if (requestDto.getImageUrls() != null && !requestDto.getImageUrls().isEmpty()) {
            List<Image> images = requestDto.getImageUrls().stream()
                    .map(url -> Image.builder()
                            .url(url)
                            .product(saved)
                            .build())
                    .toList();
            saved.getImages().addAll(images);
        }

        if (requestDto.getChildren() != null && !requestDto.getChildren().isEmpty()) {
            for (ChildLinkDto childDto : requestDto.getChildren()) {
                Product child = productRepository.findById(childDto.getChildId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy hoa con ID: " + childDto.getChildId()));

                ProductComposition comp = ProductComposition.builder()
                        .parentProduct(saved)
                        .childProduct(child)
                        .quantity(childDto.getQuantity() != null ? childDto.getQuantity() : 1)
                        .build();

                compositionRepository.save(comp);
            }
        }

        return toDto(productRepository.save(saved));
    }

    @Override
    @Transactional
    public ProductResponseDto updateProduct(Long id, ProductRequestDto requestDto) {
        Product product = productRepository.findByIdAndIsDisabledFalse(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm hoặc đã bị vô hiệu hóa: " + id));

        if (requestDto.getName() != null) product.setName(requestDto.getName());
        if (requestDto.getDescription() != null) product.setDescription(requestDto.getDescription());
        if (requestDto.getStock() != null) product.setStock(requestDto.getStock());
        if (requestDto.getPrice() != null) product.setPrice(requestDto.getPrice());
        if (requestDto.getIsPublic() != null) product.setIsPublic(requestDto.getIsPublic());
        if (requestDto.getIsCustom() != null) product.setIsCustom(requestDto.getIsCustom());
        if (requestDto.getType() != null) product.setType(requestDto.getType());

        if (requestDto.getCategoryIds() != null) {
            List<Category> categories = categoryRepository.findAllById(requestDto.getCategoryIds());
            product.getProductCategories().clear();
            List<ProductCategory> newPCs = categories.stream()
                    .map(cat -> ProductCategory.builder()
                            .product(product)
                            .category(cat)
                            .build())
                    .toList();
            product.getProductCategories().addAll(newPCs);
        }

        if (requestDto.getImageUrls() != null) {
            product.getImages().clear();
            List<Image> newImages = requestDto.getImageUrls().stream()
                    .map(url -> Image.builder().url(url).product(product).build())
                    .toList();
            product.getImages().addAll(newImages);
        }

        if (requestDto.getChildren() != null) {
            compositionRepository.deleteAllByParentProduct(product);
            product.getComponents().clear();

            for (ChildLinkDto childDto : requestDto.getChildren()) {
                Product child = productRepository.findById(childDto.getChildId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy hoa con ID: " + childDto.getChildId()));

                ProductComposition comp = ProductComposition.builder()
                        .parentProduct(product)
                        .childProduct(child)
                        .quantity(childDto.getQuantity() != null ? childDto.getQuantity() : 1)
                        .build();

                compositionRepository.save(comp);
                product.getComponents().add(comp);
            }
        }

        product.setUpdatedAt(LocalDateTime.now());
        Product saved = productRepository.save(product);
        return toDto(saved);
    }

    @Override
    public ProductResponseDto getProduct(Long id) {
        Product p = productRepository.findByIdAndIsDisabledFalse(id)
                .orElseThrow(() -> new RuntimeException("Product not found or disabled: " + id));
        return toDto(p);
    }

    @Override
    public List<ProductResponseDto> getAllActiveProducts() {
        return productRepository.findByIsDisabledFalse()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void softDeleteProduct(Long id) {
        Product product = productRepository.findByIdAndIsDisabledFalse(id)
                .orElseThrow(() -> new RuntimeException("Product not found or already disabled: " + id));
        product.setIsDisabled(true);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    @Override
    public void hardDeleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found: " + id);
        }
        productRepository.deleteById(id);
    }

    @Transactional
    public void linkProducts(LinkProductRequestDto request) {
        Product parent = productRepository.findById(request.getParentId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hoa cha ID: " + request.getParentId()));

        compositionRepository.deleteAllByParentProduct(parent);

        for (ChildLinkDto childDto : request.getChildren()) {
            Product child = productRepository.findById(childDto.getChildId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm con ID: " + childDto.getChildId()));

            ProductComposition composition = ProductComposition.builder()
                    .parentProduct(parent)
                    .childProduct(child)
                    .quantity(childDto.getQuantity() != null ? childDto.getQuantity() : 1)
                    .build();

            compositionRepository.save(composition);
        }
    }

    @Override
    public Page<ProductResponseDto> searchProductsWithType(String keyword, ProductType type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.searchProductsWithType(keyword, type, pageable);

        List<ProductResponseDto> dtos = productPage.getContent()
                .stream()
                .map(this::toDto)
                .toList();

        return new PageImpl<>(dtos, pageable, productPage.getTotalElements());
    }


    @Override
    public Page<ProductResponseDto> getProductsByFlowerCategory(Long categoryId, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findProductsByFlowerCategoryAndKeyword(categoryId, keyword, pageable);

        List<ProductResponseDto> productDtos = productPage.getContent()
                .stream()
                .map(this::toDto)
                .toList();

        return new PageImpl<>(productDtos, pageable, productPage.getTotalElements());
    }

    private ProductResponseDto toDto(Product p) {
        List<CategoryDto> categoryDtos = p.getProductCategories() == null ? List.of() :
                p.getProductCategories().stream()
                        .map(pc -> {
                            Long parentId = pc.getCategory().getParent() != null
                                    ? pc.getCategory().getParent().getId()
                                    : null;
                            return new CategoryDto(
                                    pc.getCategory().getId(),
                                    pc.getCategory().getName(),
                                    parentId
                            );
                        })
                        .toList();

        List<ImageDto> imageDtos = p.getImages() == null ? List.of() :
                p.getImages().stream()
                        .map(img -> new ImageDto(img.getId(), img.getUrl()))
                        .toList();

        List<ProductChildDto> childDtos = p.getComponents() == null ? List.of() :
                p.getComponents().stream()
                        .map(comp -> {
                            Product child = comp.getChildProduct();

                            List<CategoryDto> childCategories = child.getProductCategories() == null ? List.of() :
                                    child.getProductCategories().stream()
                                            .map(pc -> new CategoryDto(
                                                    pc.getCategory().getId(),
                                                    pc.getCategory().getName(),
                                                    pc.getCategory().getParent() != null
                                                            ? pc.getCategory().getParent().getId()
                                                            : null
                                            ))
                                            .toList();

                            List<ImageDto> childImages = child.getImages() == null ? List.of() :
                                    child.getImages().stream()
                                            .map(img -> new ImageDto(img.getId(), img.getUrl()))
                                            .toList();

                            return ProductChildDto.builder()
                                    .id(child.getId())
                                    .name(child.getName())
                                    .type(child.getType())
                                    .quantity(comp.getQuantity())
                                    .price(child.getPrice())
                                    .categories(childCategories)
                                    .images(childImages)
                                    .build();
                        })
                        .toList();

        return ProductResponseDto.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .stock(p.getStock())
                .price(p.getPrice())
                .isPublic(p.getIsPublic())
                .isCustom(p.getIsCustom())
                .isDisabled(p.getIsDisabled())
                .type(p.getType())
                .vector(p.getVector())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .categories(categoryDtos)
                .images(imageDtos)
                .children(childDtos)
                .build();
    }
}
