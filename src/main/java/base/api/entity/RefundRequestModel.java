package base.api.entity;

import base.api.enums.RefundStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "refund_requests")
public class RefundRequestModel extends BaseModel {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonManagedReference
    private OrderModel order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonManagedReference
    private UserModel user;

    @Column(nullable = false)
    private double refundAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RefundStatus status = RefundStatus.PENDING;

    @Column(length = 1000)
    private String reason; // Lý do hủy của user

    @Column(length = 1000)
    private String adminNote; // Ghi chú của admin

    @Column(length = 500)
    private String proofImageUrl; // Ảnh minh chứng hoàn tiền

    private LocalDateTime requestedAt;

    private LocalDateTime processedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private UserModel processedBy; // Admin xử lý

    @PrePersist
    protected void onCreate() {
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
    }
}
