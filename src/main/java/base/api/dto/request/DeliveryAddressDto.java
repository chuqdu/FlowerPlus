package base.api.dto.request;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class DeliveryAddressDto {
    private Long id;
    private String address;
    private boolean isDefault;
    private String recipientName;
    private String phoneNumber;
    private String province;
    private String district;
    private String ward;
    private Long userId;
}
