package com.capstone.BEApp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;

    @ManyToOne @JoinColumn(name = "flower_id")
    private Flower flower;

    @ManyToOne @JoinColumn(name = "items_id")
    private Items items;

    @ManyToOne @JoinColumn(name = "product_id")
    private Product product;
}