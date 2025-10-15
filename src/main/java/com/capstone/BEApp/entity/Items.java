package com.capstone.BEApp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Items {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double price;
    private String description;

    @OneToMany(mappedBy = "items", cascade = CascadeType.ALL)
    private List<ProductItems> productItems;

    @OneToMany(mappedBy = "items", cascade = CascadeType.ALL)
    private List<Image> images;
}