package base.api.repository;

import base.api.entity.DeliveryAddressModel;
import base.api.entity.DeliveryStatusModel;
import base.api.entity.OrderModel;
import base.api.enums.DeliveryStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IDeliveryStatusRepository extends JpaRepository<DeliveryStatusModel, Long>, JpaSpecificationExecutor<DeliveryStatusModel> {
    List<DeliveryStatusModel> findByOrderOrderByEventAtAsc(OrderModel order);
    Optional<DeliveryStatusModel> findTopByOrderOrderByEventAtDesc(OrderModel order);

    boolean existsByOrderAndStep(OrderModel order, DeliveryStep step);
    Optional<DeliveryStatusModel> findByOrderAndStep(OrderModel order, DeliveryStep step);
}
