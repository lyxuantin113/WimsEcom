package com.wims.backend.repository;

import com.wims.backend.dto.response.ProductSalesResponse;
import com.wims.backend.entity.Order;
import com.wims.backend.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;


public interface OrderRepository extends JpaRepository<Order, Long> {
    // Tìm các đơn hàng của 1 user cụ thể (Để làm trang Lịch sử mua hàng)
    Page<Order> findByUserId(Long userId, Pageable pageable);

    // Query Thống kê cho DASHBOARD
    // 1. Tính tổng doanh thu của các đơn hàng đã hoàn thành (COMPLETED)
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'COMPLETED' AND YEAR(o.createdAt) = :year")
    BigDecimal sumTotalRevenue(@Param("year") int year);

    // 2. Đếm số đơn hàng theo trạng thái
    long countByStatus(OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE YEAR(o.createdAt) = :year")
    long countOrdersByYear(@Param("year") int year);

    // 3. Tính doanh thu theo từng tháng trong năm hiện tại (Dành cho biểu đồ)
    // Trả về List các mảng Object: [Tháng, Doanh thu]
    @Query("SELECT MONTH(o.createdAt), SUM(o.totalAmount) " +
            "FROM Order o " +
            "WHERE o.status = 'COMPLETED' AND YEAR(o.createdAt) = :year " +
            "GROUP BY MONTH(o.createdAt)")
    List<Object[]> getRevenueByMonth(@Param("year") int year);

    // Lấy Top sản phẩm bán chạy trong năm (hoặc all-time nếu bỏ condition year)
    @Query("SELECT new com.wims.backend.dto.response.ProductSalesResponse(" +
            "p.id, p.name, p.image, SUM(d.quantity), SUM(d.price * d.quantity)) " +
            "FROM OrderDetail d " +
            "JOIN d.product p " +
            "JOIN d.order o " +
            "WHERE o.status = 'COMPLETED' AND YEAR(o.createdAt) = :year " +
            "GROUP BY p.id, p.name, p.image " +
            "ORDER BY SUM(d.quantity) DESC")
    List<ProductSalesResponse> findTopSellingProducts(@Param("year") int year, Pageable pageable);
}
