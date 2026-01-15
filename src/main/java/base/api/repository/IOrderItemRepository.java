package base.api.repository;

import base.api.entity.OrderItemModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface IOrderItemRepository
        extends JpaRepository<OrderItemModel, Long>, JpaSpecificationExecutor<OrderItemModel> {

    @Query(value = "SELECT " +
            "p.id as productId, " +
            "p.name as productName, " +
            "SUM(oi.quantity) as totalQuantity, " +
            "SUM(oi.line_total) as totalRevenue, " +
            "COUNT(DISTINCT oi.order_id) as orderCount " +
            "FROM order_items oi " +
            "JOIN products p ON oi.product_id = p.id " +
            "JOIN orders o ON oi.order_id = o.id " +
            "JOIN (SELECT order_id, MAX(event_at) as max_event FROM delivery_status GROUP BY order_id) latest " +
            "ON o.id = latest.order_id " +
            "JOIN delivery_status ds ON ds.order_id = latest.order_id AND ds.event_at = latest.max_event " +
            "WHERE ds.step = 'DELIVERED' " +
            "GROUP BY p.id, p.name " +
            "ORDER BY totalQuantity DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Map<String, Object>> getBestSellerProducts(int limit);
}
