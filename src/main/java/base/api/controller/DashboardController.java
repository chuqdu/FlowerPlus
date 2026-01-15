package base.api.controller;

import base.api.dto.response.SummaryDto;
import base.api.service.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final IDashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<SummaryDto> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @GetMapping("/quarterly-revenue")
    public ResponseEntity<List<Map<String, Object>>> getQuarterlyRevenueByYear(
            @RequestParam(required = false) Integer year) {
        if (year != null) {
            return ResponseEntity.ok(dashboardService.getQuarterlyRevenueByYear(year));
        } else {
            // Return all quarterly revenue if no year specified
            return ResponseEntity.ok(dashboardService.getSummary().getQuarterlyRevenue());
        }
    }
}
