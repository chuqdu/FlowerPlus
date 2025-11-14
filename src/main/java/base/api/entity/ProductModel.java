package base.api.entity;


import base.api.enums.ProductType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Data
public class ProductModel extends BaseModel {
    private String name;
    private String description;
    private double price;
    private Integer stock;
    private ProductType productType;
    private Boolean isActive = true;
    private String images;
    @Column(name = "user_id")
    private Long userId;
    private boolean isCustom = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonManagedReference
    private UserModel userModel;


    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ProductCategoryModel> productCategories = new ArrayList<>();

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ProductCompositionModel> compositions = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference("product-order-items")
    private List<OrderItemModel> orderItems = new ArrayList<>();

}
