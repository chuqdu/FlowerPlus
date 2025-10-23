package com.capstone.BEApp.service.impl;

import com.capstone.BEApp.dto.product.CreateProductDto;
import com.capstone.BEApp.dto.product.ProductDto;
import com.capstone.BEApp.entity.Image;
import com.capstone.BEApp.entity.Product;
import com.capstone.BEApp.entity.ProductFlower;
import com.capstone.BEApp.entity.ProductItems;
import com.capstone.BEApp.repository.FlowerRepository;
import com.capstone.BEApp.repository.ImageRepository;
import com.capstone.BEApp.repository.ItemRepository;
import com.capstone.BEApp.repository.ProductRepository;
import com.capstone.BEApp.service.ProductService;
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
    public ProductDto createProduct(CreateProductDto dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setStatus(dto.getStatus());
        product.setProductPrice(dto.getProductPrice());

        Product savedProduct = productRepository.save(product);

        if (dto.getFlowerIds() != null && !dto.getFlowerIds().isEmpty()) {
            List<ProductFlower> productFlowers = dto.getFlowerIds().stream()
                    .map(flowerId -> {
                        var flower = flowerRepository.findById(flowerId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy hoa id=" + flowerId));
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
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy item id=" + itemId));
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

    private ProductDto mapToDto(Product product) {
        // Lấy tên hoa
        List<String> flowerNames = product.getProductFlowers() != null
                ? product.getProductFlowers().stream()
                .map(ProductFlower::getFlower)
                .filter(f -> f != null)
                .map(f -> f.getName())
                .collect(Collectors.toList())
                : List.of();

        // Lấy tên phụ kiện
        List<String> itemNames = product.getProductItems() != null
                ? product.getProductItems().stream()
                .map(ProductItems::getItems)
                .filter(i -> i != null)
                .map(i -> i.getName())
                .collect(Collectors.toList())
                : List.of();

        // Lấy ảnh chính (ảnh đầu tiên nếu có)
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
                .flowerNames(flowerNames)
                .itemNames(itemNames)
                .build();
    }
}
