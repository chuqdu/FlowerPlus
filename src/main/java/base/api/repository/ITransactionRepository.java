package base.api.repository;

import base.api.entity.OrderItemModel;
import base.api.entity.TransactionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ITransactionRepository
                extends JpaRepository<TransactionModel, Long>, JpaSpecificationExecutor<OrderItemModel> {
        Optional<TransactionModel> findTopByOrderCodeOrderByCreatedAtDesc(String code);

        List<TransactionModel> findAllByOrderByCreatedAtDesc();

        @Query(value = "SELECT COALESCE(SUM(t.amount), 0) FROM transaction t " +
                        "JOIN orders o ON t.order_code = o.order_code " +
                        "JOIN (SELECT order_id, MAX(event_at) as max_event FROM delivery_status GROUP BY order_id) latest "
                        +
                        "ON o.id = latest.order_id " +
                        "JOIN delivery_status ds ON ds.order_id = latest.order_id AND ds.event_at = latest.max_event " +
                        "WHERE ds.step = 'DELIVERED'", nativeQuery = true)
        Double getTotalRevenue();

        @Query(value = "SELECT COALESCE(SUM(rr.refund_amount), 0) FROM refund_requests rr WHERE rr.status = 'APPROVED'", nativeQuery = true)
        Double getTotalRefunded();

        @Query(value = "SELECT TO_CHAR(t.created_at, 'YYYY-MM') as month, SUM(t.amount) as revenue " +
                        "FROM transaction t " +
                        "JOIN orders o ON t.order_code = o.order_code " +
                        "JOIN (SELECT order_id, MAX(event_at) as max_event FROM delivery_status GROUP BY order_id) latest "
                        +
                        "ON o.id = latest.order_id " +
                        "JOIN delivery_status ds ON ds.order_id = latest.order_id AND ds.event_at = latest.max_event " +
                        "WHERE ds.step = 'DELIVERED' " +
                        "GROUP BY month ORDER BY month", nativeQuery = true)
        List<Map<String, Object>> getMonthlyRevenue();

        @Query(value = "SELECT TO_CHAR(t.created_at, 'YYYY') as year, SUM(t.amount) as revenue " +
                        "FROM transaction t " +
                        "JOIN orders o ON t.order_code = o.order_code " +
                        "JOIN (SELECT order_id, MAX(event_at) as max_event FROM delivery_status GROUP BY order_id) latest "
                        +
                        "ON o.id = latest.order_id " +
                        "JOIN delivery_status ds ON ds.order_id = latest.order_id AND ds.event_at = latest.max_event " +
                        "WHERE ds.step = 'DELIVERED' " +
                        "GROUP BY year ORDER BY year", nativeQuery = true)
        List<Map<String, Object>> getYearlyRevenue();

        @Query(value = "SELECT " +
                        "TO_CHAR(t.created_at, 'YYYY') as year, " +
                        "CASE " +
                        "  WHEN EXTRACT(MONTH FROM t.created_at) IN (1, 2, 3) THEN 1 " +
                        "  WHEN EXTRACT(MONTH FROM t.created_at) IN (4, 5, 6) THEN 2 " +
                        "  WHEN EXTRACT(MONTH FROM t.created_at) IN (7, 8, 9) THEN 3 " +
                        "  WHEN EXTRACT(MONTH FROM t.created_at) IN (10, 11, 12) THEN 4 " +
                        "END as quarter, " +
                        "SUM(t.amount) as revenue " +
                        "FROM transaction t " +
                        "JOIN orders o ON t.order_code = o.order_code " +
                        "JOIN (SELECT order_id, MAX(event_at) as max_event FROM delivery_status GROUP BY order_id) latest "
                        +
                        "ON o.id = latest.order_id " +
                        "JOIN delivery_status ds ON ds.order_id = latest.order_id AND ds.event_at = latest.max_event " +
                        "WHERE ds.step = 'DELIVERED' " +
                        "GROUP BY year, quarter " +
                        "ORDER BY year, quarter", nativeQuery = true)
        List<Map<String, Object>> getQuarterlyRevenue();

        @Query(value = "SELECT " +
                        "TO_CHAR(t.created_at, 'YYYY') as year, " +
                        "CASE " +
                        "  WHEN EXTRACT(MONTH FROM t.created_at) IN (1, 2, 3) THEN 1 " +
                        "  WHEN EXTRACT(MONTH FROM t.created_at) IN (4, 5, 6) THEN 2 " +
                        "  WHEN EXTRACT(MONTH FROM t.created_at) IN (7, 8, 9) THEN 3 " +
                        "  WHEN EXTRACT(MONTH FROM t.created_at) IN (10, 11, 12) THEN 4 " +
                        "END as quarter, " +
                        "SUM(t.amount) as revenue " +
                        "FROM transaction t " +
                        "JOIN orders o ON t.order_code = o.order_code " +
                        "JOIN (SELECT order_id, MAX(event_at) as max_event FROM delivery_status GROUP BY order_id) latest "
                        +
                        "ON o.id = latest.order_id " +
                        "JOIN delivery_status ds ON ds.order_id = latest.order_id AND ds.event_at = latest.max_event " +
                        "WHERE ds.step = 'DELIVERED' " +
                        "AND EXTRACT(YEAR FROM t.created_at) = :year " +
                        "GROUP BY year, quarter " +
                        "ORDER BY year, quarter", nativeQuery = true)
        List<Map<String, Object>> getQuarterlyRevenueByYear(@Param("year") Integer year);

        Optional<TransactionModel> findByOrderCode(String orderCode);
}