package base.api.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "order_items")
public class OrderItemModel extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private OrderModel order;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    @JsonManagedReference("product-order-items")
    private ProductModel product;

    @Column(name = "product_id")
    private Long productId;

    private String productName;
    private String productImage;

    private double unitPrice;
    private int quantity;
    private double lineTotal;

    public static OrderItemModel of(Long pid, String name, String img, double price, int qty) {
        OrderItemModel x = new OrderItemModel();
        x.productId = pid;
        x.productName = name;
        x.productImage = img;
        x.unitPrice = price;
        x.quantity = qty;
        x.lineTotal = price * qty;
        return x;
    }


}