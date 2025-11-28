package base.api.controller;

import base.api.dto.response.SummaryDto;
import base.api.service.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final IDashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<SummaryDto> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }
}

