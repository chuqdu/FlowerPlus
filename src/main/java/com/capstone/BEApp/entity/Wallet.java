package com.capstone.BEApp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(precision = 18, scale = 2)
    private BigDecimal amount;

    @OneToOne
    @JoinColumn(name = "account_id")
    private Account account;
}