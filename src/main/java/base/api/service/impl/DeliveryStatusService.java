package base.api.service.impl;

import base.api.dto.request.DeliveryStatusCreateDto;
import base.api.dto.request.DeliveryStatusDto;
import base.api.entity.DeliveryStatusModel;
import base.api.entity.OrderModel;
import base.api.enums.DeliveryStep;
import base.api.repository.IDeliveryStatusRepository;
import base.api.repository.IOrderRepository;
import base.api.service.IDeliveryStatusService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class DeliveryStatusService implements IDeliveryStatusService {

    @Autowired private IDeliveryStatusRepository deliveryStatusRepository;
    @Autowired private IOrderRepository orderRepository;
    @Autowired private ModelMapper mapper;

    private static final List<DeliveryStep> LINEAR_CHAIN =
            List.of(DeliveryStep.PENDING_CONFIRMATION,
                    DeliveryStep.PREPARING,
                    DeliveryStep.DELIVERING,
                    DeliveryStep.DELIVERED);

    private List<DeliveryStep> prerequisites(DeliveryStep target) {
        if (target == DeliveryStep.DELIVERY_FAILED) {
            return List.of(DeliveryStep.PENDING_CONFIRMATION, DeliveryStep.PREPARING, DeliveryStep.DELIVERING);
        }
        int idx = LINEAR_CHAIN.indexOf(target);
        if (idx <= 0) return List.of();
        return LINEAR_CHAIN.subList(0, idx);
    }

    @Override
    public List<DeliveryStatusDto> getTimeline(Long orderId) {
        OrderModel order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        return deliveryStatusRepository.findByOrderOrderByEventAtAsc(order)
                .stream().map(e -> mapper.map(e, DeliveryStatusDto.class)).toList();
    }

    @Override
    @Transactional
    public DeliveryStatusDto setCurrentStepCascading(Long orderId,
                                                     DeliveryStep targetStep,
                                                     String note,
                                                     String location,
                                                     String imageUrl,
                                                     Long userId) {
        OrderModel order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        List<DeliveryStatusModel> created = new ArrayList<>();
        for (DeliveryStep pre : prerequisites(targetStep)) {
            if (!deliveryStatusRepository.existsByOrderAndStep(order, pre)) {
                DeliveryStatusModel m = new DeliveryStatusModel();
                m.setOrder(order);
                m.setStep(pre);
                m.setEventAt(OffsetDateTime.now());
                m.setUserId(userId);
                deliveryStatusRepository.save(m);
                created.add(m);
            }
        }

        DeliveryStatusModel target = deliveryStatusRepository.findByOrderAndStep(order, targetStep)
                .orElseGet(() -> {
                    DeliveryStatusModel m = new DeliveryStatusModel();
                    m.setOrder(order);
                    m.setStep(targetStep);
                    return m;
                });
        if (target.getId() == null) {
            target.setEventAt(OffsetDateTime.now());
        }
        target.setNote(note);
        target.setLocation(location);
        target.setImageUrl(imageUrl);
        target.setUserId(userId);

        deliveryStatusRepository.save(target);

        return mapper.map(target, DeliveryStatusDto.class);
    }

    @Override
    @Transactional
    public DeliveryStatusDto appendStatus(Long orderId, DeliveryStatusCreateDto dto, Long userId) {
        if (dto.getStep() == null) throw new IllegalArgumentException("Step is required");
        return setCurrentStepCascading(orderId, dto.getStep(), dto.getNote(), dto.getLocation(), dto.getImageUrl(), userId);
    }

    @Override
    @Transactional
    public void updateDeliveryStatusImage(Long deliveryStatusId, String imageUrl) {
        DeliveryStatusModel deliveryStatus = deliveryStatusRepository.findById(deliveryStatusId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery status not found: " + deliveryStatusId));
        
        deliveryStatus.setImageUrl(imageUrl);
        deliveryStatusRepository.save(deliveryStatus);
    }
}
