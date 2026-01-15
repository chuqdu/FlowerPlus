package base.api.dto.response;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class SummaryDto {
    private long totalUsers;
    private long totalProducts;
    private long totalOrders;
    private double totalRevenue;
    private double totalRefunded;
    private double netRevenue;
    
    private long successfulOrders;
    private long deliveringOrders;
    private long pendingOrders;
    private long failedOrders;
    private long refundedOrders;

    private Map<String, Object> orderAmountsByStatus;
    private List<Map<String, Object>> monthlyOrders;
    private List<Map<String, Object>> monthlyOrdersByStatus;
    private List<Map<String, Object>> yearlyOrders;
    private List<Map<String, Object>> monthlyRevenue;
    private List<Map<String, Object>> yearlyRevenue;
    private List<Map<String, Object>> quarterlyRevenue;
    private List<Map<String, Object>> bestSellerProducts;
    private List<Map<String, Object>> topCustomers;
}
