package com.capstone.BEApp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_compositions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductComposition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_product_id")
    private Product parentProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_product_id")
    private Product childProduct;

    @Column(nullable = false)
    private Integer quantity = 1;
}

