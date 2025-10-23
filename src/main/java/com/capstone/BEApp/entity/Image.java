package com.capstone.BEApp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor @AllArgsConstructor @Builder
@Getter
@Setter
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String url;

    @ManyToOne @JoinColumn(name = "flower_id")
    private Flower flower;

    @ManyToOne @JoinColumn(name = "items_id")
    private Items items;

    @ManyToOne @JoinColumn(name = "product_id")
    private Product product;
}