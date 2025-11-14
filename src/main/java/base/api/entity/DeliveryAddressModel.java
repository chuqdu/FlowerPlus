package base.api.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "delivery_addresses")
public class DeliveryAddressModel  extends BaseModel  {
    private String address;
    private boolean isDefault;
    private String recipientName;
    private String phoneNumber;
    private String province;
    private String district;
    private String ward;

    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonBackReference
    private UserModel userModel;
}
