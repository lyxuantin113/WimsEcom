package com.wims.backend.service;

import com.wims.backend.dto.response.DashboardResponse;
import com.wims.backend.dto.response.MonthlyRevenue;
import com.wims.backend.dto.response.ProductSalesResponse;
import com.wims.backend.repository.OrderRepository;
import com.wims.backend.repository.ProductRepository;
import com.wims.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public DashboardResponse getStats(Integer currentYear) {

        // 1. Các số liệu tổng quan
        BigDecimal revenue = orderRepository.sumTotalRevenue(currentYear);
        long totalOrders = orderRepository.countOrdersByYear(currentYear);
        long totalProducts = productRepository.countProductByYear(currentYear);
        long totalUsers = userRepository.countUsersByYear(currentYear);

        // 2. Xử lý dữ liệu biểu đồ
        List<Object[]> revenueData = orderRepository.getRevenueByMonth(currentYear);
        List<MonthlyRevenue> chartData = new ArrayList<>();

        // Khởi tạo đủ 12 tháng là 0 để biểu đồ không bị đứt đoạn
        for (int i = 1; i <= 12; i++) {
            chartData.add(new MonthlyRevenue(i, BigDecimal.ZERO));
        }

        // Fill dữ liệu thật vào
        for (Object[] row : revenueData) {
            int month = (int) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            chartData.set(month - 1, new MonthlyRevenue(month, amount));
        }

        // Gọi Repo lấy Top 5
        Pageable topFive = PageRequest.of(0, 5);
        List<ProductSalesResponse> topProducts = orderRepository.findTopSellingProducts(currentYear, topFive);

        return DashboardResponse.builder()
                .totalRevenue(revenue == null ? BigDecimal.ZERO : revenue)
                .totalOrders(totalOrders)
                .totalProducts(totalProducts)
                .totalUsers(totalUsers)
                .revenueByMonth(chartData)
                .topProducts(topProducts)
                .build();
    }
}
