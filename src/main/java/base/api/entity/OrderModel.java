package base.api.entity;

import base.api.enums.OrderType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
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
    private LocalDateTime requestDeliveryTime;

    // Thông tin voucher áp dụng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    private VoucherModel voucher;

    private String voucherCode;
    private double discountAmount = 0.0;

    private double total; // total = subtotal - discount

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

    private boolean cancelled = false;

    private LocalDateTime cancelledAt;

    public void addDeliveryStatus(DeliveryStatusModel s) {
        this.deliveryStatuses.add(s);
        s.setOrder(this);
    }

    public void addItem(OrderItemModel it) {
        items.add(it);
        it.setOrder(this);
    }

    public double calcItemsSubtotal() {
        return items.stream().mapToDouble(OrderItemModel::getLineTotal).sum();
    }

    public void recalcTotal() {
        this.total = calcItemsSubtotal() - (discountAmount == 0 ? 0 : discountAmount);
        if (this.total < 0) this.total = 0;
    }
}
