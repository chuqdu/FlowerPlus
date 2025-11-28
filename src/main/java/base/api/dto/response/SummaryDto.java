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

    private List<Map<String, Object>> monthlyOrders;
    private List<Map<String, Object>> yearlyOrders;
    private List<Map<String, Object>> monthlyRevenue;
    private List<Map<String, Object>> yearlyRevenue;
}
