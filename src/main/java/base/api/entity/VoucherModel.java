package base.api.entity;

import base.api.enums.VoucherType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "vouchers")
public class VoucherModel extends BaseModel {

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VoucherType type; // PERCENTAGE or FIXED

    private Double percent;

    private Double amount;

    private Double minOrderValue;

    private Double maxDiscountAmount;

    private LocalDateTime startsAt;
    private LocalDateTime endsAt;

    private Integer usageLimit;
    private Integer usedCount = 0;

    private Boolean applyAllProducts = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "voucher_products",
            joinColumns = @JoinColumn(name = "voucher_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    @JsonBackReference
    private Set<ProductModel> products = new HashSet<>();
}
