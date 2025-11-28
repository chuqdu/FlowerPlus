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

    @Query(value = "SELECT TO_CHAR(created_at, 'YYYY-MM') as month, COUNT(*) as orderCount FROM orders GROUP BY month ORDER BY month", nativeQuery = true)
    List<Map<String, Object>> countMonthlyOrders();

    @Query(value = "SELECT TO_CHAR(created_at, 'YYYY') as year, COUNT(*) as orderCount FROM orders GROUP BY year ORDER BY year", nativeQuery = true)
    List<Map<String, Object>> countYearlyOrders();
}
