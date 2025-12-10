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

    @Query(value = "SELECT COALESCE(SUM(t.amount), 0) FROM transaction t " +
            "JOIN orders o ON t.order_code = o.order_code " +
            "JOIN (SELECT order_id, MAX(event_at) as max_event FROM delivery_status GROUP BY order_id) latest " +
            "ON o.id = latest.order_id " +
            "JOIN delivery_status ds ON ds.order_id = latest.order_id AND ds.event_at = latest.max_event " +
            "WHERE ds.step = 'DELIVERED'", nativeQuery = true)
    Double getTotalRevenue();

    @Query(value = "SELECT COALESCE(SUM(rr.refund_amount), 0) FROM refund_requests rr WHERE rr.status = 'APPROVED'", nativeQuery = true)
    Double getTotalRefunded();

    @Query(value = "SELECT TO_CHAR(t.created_at, 'YYYY-MM') as month, SUM(t.amount) as revenue " +
            "FROM transaction t " +
            "JOIN orders o ON t.order_code = o.order_code " +
            "JOIN (SELECT order_id, MAX(event_at) as max_event FROM delivery_status GROUP BY order_id) latest " +
            "ON o.id = latest.order_id " +
            "JOIN delivery_status ds ON ds.order_id = latest.order_id AND ds.event_at = latest.max_event " +
            "WHERE ds.step = 'DELIVERED' " +
            "GROUP BY month ORDER BY month", nativeQuery = true)
    List<Map<String, Object>> getMonthlyRevenue();

    @Query(value = "SELECT TO_CHAR(t.created_at, 'YYYY') as year, SUM(t.amount) as revenue " +
            "FROM transaction t " +
            "JOIN orders o ON t.order_code = o.order_code " +
            "JOIN (SELECT order_id, MAX(event_at) as max_event FROM delivery_status GROUP BY order_id) latest " +
            "ON o.id = latest.order_id " +
            "JOIN delivery_status ds ON ds.order_id = latest.order_id AND ds.event_at = latest.max_event " +
            "WHERE ds.step = 'DELIVERED' " +
            "GROUP BY year ORDER BY year", nativeQuery = true)
    List<Map<String, Object>> getYearlyRevenue();

    Optional<TransactionModel> findByOrderCode(String orderCode);
}