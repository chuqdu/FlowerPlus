package base.api.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cart_items")
@EqualsAndHashCode(callSuper = true)
public class CartItemModel extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonBackReference
    private CartModel cart;

    @Column(nullable = false)
    private Long productId;

    @Column(length = 255)
    private String productName;

    private String productImage;

    private double  unitPrice;

    @Column(nullable = false)
    private Integer quantity = 1;

    private double  lineTotal;

    // Mốc thời gian tiện theo dõi
    private LocalDateTime addedAt = LocalDateTime.now();

    public void recalcLineTotal() {
        this.lineTotal = unitPrice * quantity;
    }
}
