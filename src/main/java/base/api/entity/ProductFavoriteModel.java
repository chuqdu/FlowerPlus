package base.api.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "product_favorites", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
@EqualsAndHashCode(callSuper = true)
public class ProductFavoriteModel extends BaseModel {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonBackReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UserModel user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    @JsonBackReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ProductModel product;

    public ProductFavoriteModel() {}

    public ProductFavoriteModel(Long userId, Long productId) {
        this.userId = userId;
        this.productId = productId;
    }
}
