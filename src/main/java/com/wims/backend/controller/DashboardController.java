package com.wims.backend.controller;

import com.wims.backend.dto.ApiResponse;
import com.wims.backend.dto.response.DashboardResponse;
import com.wims.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DashboardResponse> getStats(
            @RequestParam(required = false) Integer year
    ) {
        int selectedYear = (year == null) ? LocalDate.now().getYear() : year;

        return ApiResponse.<DashboardResponse>builder()
                .result(dashboardService.getStats(selectedYear))
                .build();
    }
}