package com.capstone.BEApp.service.impl;

import com.capstone.BEApp.constant.ProductStatus;
import com.capstone.BEApp.dto.flower.FlowerDto;
import com.capstone.BEApp.dto.item.ItemDto;
import com.capstone.BEApp.dto.product.CreateProductDto;
import com.capstone.BEApp.dto.product.ProductDto;
import com.capstone.BEApp.dto.product.UpdateProductDto;
import com.capstone.BEApp.entity.*;
import com.capstone.BEApp.repository.*;
import com.capstone.BEApp.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final FlowerRepository flowerRepository;
    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;

    private final ProductCategoryRepository productCategoryRepository;
    private final ProductFlowerRepository productFlowerRepository;
    private final ProductItemsRepository productItemsRepository;
    private final ImageRepository imageRepository;

    @Override
    public Page<ProductDto> searchProducts(
            String keyword,
            String status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable,
            Long categoryId
    ) {
        Page<Product> products = productRepository.searchProducts(
                keyword,
                status,
                minPrice,
                maxPrice,
                categoryId,
                pageable
        );
        return products.map(this::mapToDto);
    }


    @Override
    @Transactional
    public ProductDto createProduct(CreateProductDto dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setStatus(dto.getStatus() != null ? dto.getStatus() : ProductStatus.ACTIVE);
        product.setProductPrice(dto.getProductPrice());

        Product savedProduct = productRepository.save(product);

        if (dto.getCategoryId() != null && !dto.getCategoryId().isEmpty()) {
            List<ProductCategory> productCategories = dto.getCategoryId().stream()
                    .map(categoryId -> {
                        var category = categoryRepository.findById(categoryId)
                                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy category id=" + categoryId));
                        return ProductCategory.builder()
                                .product(savedProduct)
                                .category(category)
                                .build();
                    })
                    .collect(Collectors.toList());
            savedProduct.setProductCategories(productCategories);
        }

        if (dto.getFlowerIds() != null && !dto.getFlowerIds().isEmpty()) {
            List<ProductFlower> productFlowers = dto.getFlowerIds().stream()
                    .map(flowerId -> {
                        var flower = flowerRepository.findById(flowerId)
                                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hoa id=" + flowerId));
                        return ProductFlower.builder()
                                .product(savedProduct)
                                .flower(flower)
                                .build();
                    })
                    .collect(Collectors.toList());
            savedProduct.setProductFlowers(productFlowers);
        }

        if (dto.getItemIds() != null && !dto.getItemIds().isEmpty()) {
            List<ProductItems> productItems = dto.getItemIds().stream()
                    .map(itemId -> {
                        var item = itemRepository.findById(itemId)
                                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy item id=" + itemId));
                        return ProductItems.builder()
                                .product(savedProduct)
                                .items(item)
                                .build();
                    })
                    .collect(Collectors.toList());
            savedProduct.setProductItems(productItems);
        }

        if (dto.getImageUrls() != null && !dto.getImageUrls().isEmpty()) {
            List<Image> images = dto.getImageUrls().stream()
                    .map(url -> Image.builder()
                            .url(url)
                            .product(savedProduct)
                            .build())
                    .collect(Collectors.toList());
            savedProduct.setImages(images);
        }

        Product finalSaved = productRepository.save(savedProduct);
        return mapToDto(finalSaved);
    }

    @Override
    @Transactional
    public ProductDto updateProduct( UpdateProductDto dto) {
        Product product = productRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm id=" + dto.getId()));

        if (dto.getName() != null) product.setName(dto.getName());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getStatus() != null) product.setStatus(dto.getStatus());
        if (dto.getProductPrice() != null) product.setProductPrice(dto.getProductPrice());

        if (dto.getCategoryId() != null) {
            productCategoryRepository.deleteAllByProductId(product.getId());

            List<ProductCategory> productCategories = dto.getCategoryId().stream()
                    .map(categoryId -> {
                        var category = categoryRepository.findById(categoryId)
                                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy category id=" + categoryId));
                        return ProductCategory.builder()
                                .product(product)
                                .category(category)
                                .build();
                    })
                    .collect(Collectors.toList());
            product.setProductCategories(productCategories);
        }

        if (dto.getFlowerIds() != null) {
            productFlowerRepository.deleteAllByProductId(product.getId());

            List<ProductFlower> productFlowers = dto.getFlowerIds().stream()
                    .map(flowerId -> {
                        var flower = flowerRepository.findById(flowerId)
                                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hoa id=" + flowerId));
                        return ProductFlower.builder()
                                .product(product)
                                .flower(flower)
                                .build();
                    })
                    .collect(Collectors.toList());
            product.setProductFlowers(productFlowers);
        }

        if (dto.getItemIds() != null) {
            productItemsRepository.deleteAllByProductId(product.getId());

            List<ProductItems> productItems = dto.getItemIds().stream()
                    .map(itemId -> {
                        var item = itemRepository.findById(itemId)
                                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy item id=" + itemId));
                        return ProductItems.builder()
                                .product(product)
                                .items(item)
                                .build();
                    })
                    .collect(Collectors.toList());
            product.setProductItems(productItems);
        }

        if (dto.getImageUrls() != null) {
            imageRepository.deleteAllByProductId(product.getId());

            List<Image> images = dto.getImageUrls().stream()
                    .map(url -> Image.builder()
                            .url(url)
                            .product(product)
                            .build())
                    .collect(Collectors.toList());
            product.setImages(images);
        }

        Product updated = productRepository.save(product);
        return mapToDto(updated);
    }

    private ProductDto mapToDto(Product product) {
        List<FlowerDto> flowers = product.getProductFlowers() != null
                ? product.getProductFlowers().stream()
                .map(ProductFlower::getFlower)
                .filter(f -> f != null)
                .map(f -> FlowerDto.builder()
                        .id(f.getId())
                        .name(f.getName())
                        .description(f.getDescription())
                        .price(f.getPrice())
                        .quality(f.getQuality())
                        .status(f.getStatus())
                        .season(f.getSeason())
                        .createdDate(f.getCreatedDate())
                        .imageUrls(f.getImages() != null
                                ? f.getImages().stream()
                                .map(img -> img.getUrl())
                                .collect(Collectors.toList())
                                : List.of())
                        .build())
                .collect(Collectors.toList())
                : List.of();

        List<ItemDto> items = product.getProductItems() != null
                ? product.getProductItems().stream()
                .map(ProductItems::getItems)
                .filter(i -> i != null)
                .map(i -> ItemDto.builder()
                        .id(i.getId())
                        .name(i.getName())
                        .price(i.getPrice())
                        .description(i.getDescription())
                        .imageUrls(i.getImages() != null
                                ? i.getImages().stream()
                                .map(img -> img.getUrl())
                                .collect(Collectors.toList())
                                : List.of())
                        .build())
                .collect(Collectors.toList())
                : List.of();

        String mainImageUrl = (product.getImages() != null && !product.getImages().isEmpty())
                ? product.getImages().get(0).getUrl()
                : null;

        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .status(product.getStatus())
                .productPrice(product.getProductPrice())
                .mainImageUrl(mainImageUrl)
                .flowers(flowers)
                .items(items)
                .build();
    }

}
