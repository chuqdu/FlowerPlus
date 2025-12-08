package base.api.repository;

import base.api.entity.RefundRequestModel;
import base.api.enums.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IRefundRequestRepository extends JpaRepository<RefundRequestModel, Long> {
    Optional<RefundRequestModel> findByOrder_Id(Long orderId);
    List<RefundRequestModel> findByStatus(RefundStatus status);
    List<RefundRequestModel> findByUser_IdOrderByRequestedAtDesc(Long userId);
    List<RefundRequestModel> findAllByOrderByRequestedAtDesc();
}
