package com.capstone.BEApp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Flower {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String quality;
    private String description;
    @Column(precision = 18, scale = 2)
    private BigDecimal price;
    private String status;
    private String season;
    private LocalDateTime createdDate = LocalDateTime.now();

    @OneToMany(mappedBy = "flower", cascade = CascadeType.ALL)
    private List<ProductFlower> productFlowers;

    @OneToMany(mappedBy = "flower", cascade = CascadeType.ALL)
    private List<Image> images;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

}