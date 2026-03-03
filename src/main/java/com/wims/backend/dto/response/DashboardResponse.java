package com.wims.backend.dto.response;

import lombok.Builder;
import java.math.BigDecimal;
import java.util.List;

@Builder
public record DashboardResponse(
        BigDecimal totalRevenue,
        long totalOrders,
        long totalProducts,
        long totalUsers,
        List<MonthlyRevenue> revenueByMonth,
        List<ProductSalesResponse> topProducts) {
}
