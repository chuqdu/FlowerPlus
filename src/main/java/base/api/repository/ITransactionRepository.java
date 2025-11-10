package base.api.repository;

import base.api.entity.OrderItemModel;
import base.api.entity.TransactionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ITransactionRepository  extends JpaRepository<TransactionModel, Long>, JpaSpecificationExecutor<OrderItemModel> {
    Optional<TransactionModel> findTopByOrderCodeOrderByCreatedAtDesc(String code);
    List<TransactionModel> findAllByOrderByCreatedAtDesc();
}