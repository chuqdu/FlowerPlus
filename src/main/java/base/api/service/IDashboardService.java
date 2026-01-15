package base.api.service;

import base.api.dto.response.SummaryDto;

import java.util.List;
import java.util.Map;

public interface IDashboardService {
    SummaryDto getSummary();

    List<Map<String, Object>> getQuarterlyRevenueByYear(Integer year);
}
