package base.api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "transaction") // escape để tránh đụng từ khóa DB
public class TransactionModel extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderModel order;  // <-- TÊN FIELD PHẢI LÀ 'order'

    @Column(nullable = false)
    private String orderCode;

    private double amount;

    @Column(length = 20)
    private String status = "PENDING"; // PENDING | SUCCESS | FAILED | CANCELED | EXPIRED

    private String checkoutUrl;
    private String paymentLinkId;
}
