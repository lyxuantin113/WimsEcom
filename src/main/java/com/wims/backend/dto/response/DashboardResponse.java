package com.wims.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardResponse {
    private BigDecimal totalRevenue;
    private long totalOrders;
    private long totalProducts;
    private long totalUsers;

    // Dữ liệu cho biểu đồ
    private List<MonthlyRevenue> revenueByMonth;

    private List<ProductSalesResponse> topProducts;
}
