package base.api.repository;

import base.api.entity.OrderModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface IOrderRepository extends JpaRepository<OrderModel, Long>, JpaSpecificationExecutor<OrderModel> {
    Optional<OrderModel> findByOrderCode(String code);
    List<OrderModel> findByUser_Id(Long userId);
    List<OrderModel> findAllByOrderByCreatedAtDesc();
        List<OrderModel> findByUser_IdOrderByCreatedAtDesc(Long userId);

    @Query(value = "SELECT TO_CHAR(o.created_at, 'YYYY-MM') as month, COUNT(DISTINCT o.id) as orderCount " +
            "FROM orders o " +
            "JOIN (SELECT order_id, MAX(event_at) as max_event FROM delivery_status GROUP BY order_id) latest " +
            "ON o.id = latest.order_id " +
            "JOIN delivery_status ds ON ds.order_id = latest.order_id AND ds.event_at = latest.max_event " +
            "WHERE ds.step = 'DELIVERED' " +
            "GROUP BY month ORDER BY month", nativeQuery = true)
    List<Map<String, Object>> countMonthlyOrders();

    @Query(value = "SELECT TO_CHAR(o.created_at, 'YYYY') as year, COUNT(DISTINCT o.id) as orderCount " +
            "FROM orders o " +
            "JOIN (SELECT order_id, MAX(event_at) as max_event FROM delivery_status GROUP BY order_id) latest " +
            "ON o.id = latest.order_id " +
            "JOIN delivery_status ds ON ds.order_id = latest.order_id AND ds.event_at = latest.max_event " +
            "WHERE ds.step = 'DELIVERED' " +
            "GROUP BY year ORDER BY year", nativeQuery = true)
    List<Map<String, Object>> countYearlyOrders();

    @Query(value = "SELECT COUNT(DISTINCT o.id) " +
            "FROM orders o " +
            "JOIN (SELECT order_id, MAX(event_at) as max_event FROM delivery_status GROUP BY order_id) latest " +
            "ON o.id = latest.order_id " +
            "JOIN delivery_status ds ON ds.order_id = latest.order_id AND ds.event_at = latest.max_event " +
            "WHERE ds.step = 'DELIVERED'", nativeQuery = true)
    Long countSuccessfulOrders();

    @Query(value = "SELECT COUNT(DISTINCT o.id) " +
            "FROM orders o " +
            "JOIN (SELECT order_id, MAX(event_at) as max_event FROM delivery_status GROUP BY order_id) latest " +
            "ON o.id = latest.order_id " +
            "JOIN delivery_status ds ON ds.order_id = latest.order_id AND ds.event_at = latest.max_event " +
            "WHERE ds.step = 'DELIVERING'", nativeQuery = true)
    Long countDeliveringOrders();

    @Query(value = "SELECT COUNT(DISTINCT o.id) " +
            "FROM orders o " +
            "JOIN (SELECT order_id, MAX(event_at) as max_event FROM delivery_status GROUP BY order_id) latest " +
            "ON o.id = latest.order_id " +
            "JOIN delivery_status ds ON ds.order_id = latest.order_id AND ds.event_at = latest.max_event " +
            "WHERE ds.step IN ('PENDING_CONFIRMATION', 'PREPARING')", nativeQuery = true)
    Long countPendingOrders();

    @Query(value = "SELECT COUNT(DISTINCT o.id) " +
            "FROM orders o " +
            "JOIN (SELECT order_id, MAX(event_at) as max_event FROM delivery_status GROUP BY order_id) latest " +
            "ON o.id = latest.order_id " +
            "JOIN delivery_status ds ON ds.order_id = latest.order_id AND ds.event_at = latest.max_event " +
            "WHERE ds.step IN ('CANCELLED', 'DELIVERY_FAILED')", nativeQuery = true)
    Long countFailedOrders();

    @Query(value = "SELECT COUNT(DISTINCT rr.id) FROM refund_requests rr WHERE rr.status = 'APPROVED'", nativeQuery = true)
    Long countRefundedOrders();

    @Query(value = "SELECT " +
            "SUM(CASE WHEN ds.step = 'DELIVERED' THEN o.total ELSE 0 END) as deliveredAmount, " +
            "SUM(CASE WHEN ds.step = 'DELIVERING' THEN o.total ELSE 0 END) as deliveringAmount, " +
            "SUM(CASE WHEN ds.step IN ('PENDING_CONFIRMATION', 'PREPARING') THEN o.total ELSE 0 END) as pendingAmount, " +
            "SUM(CASE WHEN ds.step IN ('CANCELLED', 'DELIVERY_FAILED') THEN o.total ELSE 0 END) as failedAmount " +
            "FROM orders o " +
            "JOIN (SELECT order_id, MAX(event_at) as max_event FROM delivery_status GROUP BY order_id) latest " +
            "ON o.id = latest.order_id " +
            "JOIN delivery_status ds ON ds.order_id = latest.order_id AND ds.event_at = latest.max_event", nativeQuery = true)
    Map<String, Object> getOrderAmountsByStatus();

    @Query(value = "SELECT u.id, CONCAT(COALESCE(u.first_name, ''), ' ', COALESCE(u.last_name, '')) as name, u.email, " +
            "COUNT(DISTINCT o.id) as orderCount, SUM(o.total) as totalSpent " +
            "FROM orders o " +
            "JOIN user u ON o.user_id = u.id " +
            "JOIN (SELECT order_id, MAX(event_at) as max_event FROM delivery_status GROUP BY order_id) latest " +
            "ON o.id = latest.order_id " +
            "JOIN delivery_status ds ON ds.order_id = latest.order_id AND ds.event_at = latest.max_event " +
            "WHERE ds.step = 'DELIVERED' " +
            "GROUP BY u.id, u.first_name, u.last_name, u.email " +
            "ORDER BY totalSpent DESC " +
            "LIMIT 5", nativeQuery = true)
    List<Map<String, Object>> getTopCustomers();

    @Query(value = "SELECT " +
            "TO_CHAR(o.created_at, 'YYYY-MM') as month, " +
            "SUM(CASE WHEN ds.step = 'DELIVERED' THEN 1 ELSE 0 END) as delivered, " +
            "SUM(CASE WHEN ds.step = 'DELIVERING' THEN 1 ELSE 0 END) as delivering, " +
            "SUM(CASE WHEN ds.step IN ('PENDING_CONFIRMATION', 'PREPARING') THEN 1 ELSE 0 END) as pending " +
            "FROM orders o " +
            "JOIN (SELECT order_id, MAX(event_at) as max_event FROM delivery_status GROUP BY order_id) latest " +
            "ON o.id = latest.order_id " +
            "JOIN delivery_status ds ON ds.order_id = latest.order_id AND ds.event_at = latest.max_event " +
            "GROUP BY month " +
            "ORDER BY month", nativeQuery = true)
    List<Map<String, Object>> getMonthlyOrdersByStatus();
}
