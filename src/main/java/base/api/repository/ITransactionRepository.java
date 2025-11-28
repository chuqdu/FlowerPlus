package base.api.repository;

import base.api.entity.OrderItemModel;
import base.api.entity.TransactionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ITransactionRepository  extends JpaRepository<TransactionModel, Long>, JpaSpecificationExecutor<OrderItemModel> {
    Optional<TransactionModel> findTopByOrderCodeOrderByCreatedAtDesc(String code);
        List<TransactionModel> findAllByOrderByCreatedAtDesc();

    @Query(value = "SELECT SUM(amount) FROM transaction", nativeQuery = true)
    Double getTotalRevenue();

    @Query(value = "SELECT TO_CHAR(created_at, 'YYYY-MM') as month, SUM(amount) as revenue FROM transaction GROUP BY month ORDER BY month", nativeQuery = true)
    List<Map<String, Object>> getMonthlyRevenue();

    @Query(value = "SELECT TO_CHAR(created_at, 'YYYY') as year, SUM(amount) as revenue FROM transaction GROUP BY year ORDER BY year", nativeQuery = true)
    List<Map<String, Object>> getYearlyRevenue();
}