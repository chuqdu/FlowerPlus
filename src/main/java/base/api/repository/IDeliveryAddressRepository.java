package base.api.repository;

import base.api.entity.CategoryModel;
import base.api.entity.DeliveryAddressModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IDeliveryAddressRepository extends JpaRepository<DeliveryAddressModel, Long>, JpaSpecificationExecutor<DeliveryAddressModel> {
    Optional<DeliveryAddressModel> findByIdAndUserId(Long id, Long userId);
    List<DeliveryAddressModel> findByUserId(Long userId);
    List<DeliveryAddressModel> findByUserIdAndIsDefaultTrue(Long userId);
}
