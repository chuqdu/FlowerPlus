package base.api.entity;

import base.api.enums.OrderType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "orders")
public class OrderModel extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonManagedReference
    private UserModel user;

    @Column(nullable = false, unique = true)
    private String orderCode;
    private String note;
    private String shippingAddress;
    private String phoneNumber;

    private double total;

    @OneToOne(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            optional = false
    )
    @JsonManagedReference
    private TransactionModel transaction;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<OrderItemModel> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<DeliveryStatusModel> deliveryStatuses = new ArrayList<>();

    public void addDeliveryStatus(DeliveryStatusModel s) {
        this.deliveryStatuses.add(s);
        s.setOrder(this);
    }

    public void addItem(OrderItemModel it) {
        items.add(it);
        it.setOrder(this);
    }


    public void recalcTotal() {
        this.total = items.stream().mapToDouble(OrderItemModel::getLineTotal).sum();
    }
}
