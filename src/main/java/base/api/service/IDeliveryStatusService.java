package base.api.service;

import base.api.dto.request.DeliveryStatusCreateDto;
import base.api.dto.request.DeliveryStatusDto;
import base.api.enums.DeliveryStep;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IDeliveryStatusService {
    List<DeliveryStatusDto> getTimeline(Long orderId);

    DeliveryStatusDto setCurrentStepCascading(
            Long orderId,
            DeliveryStep targetStep,
            String note,
            String location,
            String imageUrl,
            Long userId
    );

    DeliveryStatusDto appendStatus(Long orderId, DeliveryStatusCreateDto dto, Long userId);
}
