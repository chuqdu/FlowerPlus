package base.api.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "product_compositions")
public class ProductCompositionModel extends BaseModel {
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_product_id", nullable = false)
    @JsonBackReference
    private ProductModel parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_product_id", nullable = false)
    @JsonBackReference
    private ProductModel child;
}