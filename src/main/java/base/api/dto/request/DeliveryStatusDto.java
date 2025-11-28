package base.api.dto.request;

import base.api.enums.DeliveryStep;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class DeliveryStatusDto {
    private Long id;
    private DeliveryStep step;
    private OffsetDateTime eventAt;
    private String note;
    private String location;
    private String imageUrl;
    private Long userId;
}