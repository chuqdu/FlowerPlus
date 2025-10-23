package com.capstone.BEApp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", columnDefinition = "NVARCHAR(255)")
    private String name;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String avatar;
    private String email;
    private String phone;
    private Integer age;
    private String gender;
    private String password;
    @Column(name = "address", columnDefinition = "NVARCHAR(255)")
    private String address;
    private LocalDateTime createdDate = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    @JsonIgnore
    private Role role;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<Order> orders;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<Cart> carts;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    private Wallet wallet;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Transaction> transactions;
}
