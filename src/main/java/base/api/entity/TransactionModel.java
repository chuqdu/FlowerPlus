package base.api.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "transaction")
public class TransactionModel extends BaseModel {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    @JsonBackReference
    private OrderModel order;

    @Column(nullable = false)
    private String orderCode;

    private double amount;

    @Column(length = 20)
    private String status = "PENDING"; // PENDING | SUCCESS | FAILED | CANCELED | EXPIRED

    private String checkoutUrl;
    private String paymentLinkId;
}
