package base.api.repository;

import base.api.entity.OrderModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IOrderRepository extends JpaRepository<OrderModel, Long>, JpaSpecificationExecutor<OrderModel> {
    Optional<OrderModel> findByOrderCode(String code);
    List<OrderModel> findByUser_Id(Long userId);
    List<OrderModel> findAllByOrderByCreatedAtDesc();
    List<OrderModel> findByUser_IdOrderByCreatedAtDesc(Long userId);
}
