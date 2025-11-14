package base.api.entity;


import base.api.enums.DeliveryStep;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;

@Data
@Entity
@Table(
        name = "delivery_status",
        indexes = {
                @Index(name = "idx_delivery_status_order", columnList = "order_id"),
                @Index(name = "idx_delivery_status_event_at", columnList = "event_at"),
                @Index(name = "idx_delivery_status_step", columnList = "step")
        }
)
@EqualsAndHashCode(callSuper = true)
public class DeliveryStatusModel extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private OrderModel order;

    @Enumerated(EnumType.STRING)
    @Column(name = "step", nullable = false, length = 40)
    private DeliveryStep step;

    @Column(name = "event_at", nullable = false)
    private OffsetDateTime eventAt = OffsetDateTime.now();

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonBackReference
    private UserModel userModel;



}