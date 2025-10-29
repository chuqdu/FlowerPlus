package com.capstone.BEApp.entity;

import com.capstone.BEApp.entity.enumFile.ProductType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "text")
    private String description;

    private Integer stock;

    @Column(name = "price", precision = 18, scale = 2)
    private BigDecimal price;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_public")
    private Boolean isPublic = Boolean.TRUE;
    @Column(name = "is_custom")
    private Boolean isCustom = Boolean.FALSE;


    @Column(name = "is_disabled")
    private Boolean isDisabled = Boolean.FALSE;

    @Column(columnDefinition = "json")
    private String vector;

    @OneToMany(mappedBy = "parentProduct", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductComposition> components = new ArrayList<>();

    @OneToMany(mappedBy = "childProduct", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductComposition> usedInProducts = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20)
    private ProductType type = ProductType.PRODUCT;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Image> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductCategory> productCategories = new ArrayList<>();

}
