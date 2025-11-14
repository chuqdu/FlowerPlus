package base.api.dto.request;


import base.api.enums.DeliveryStep;
import lombok.Data;


@Data
public class DeliveryStatusCreateDto {
    private DeliveryStep step;
    private String note;
    private String location;
    private String imageUrl;
}
