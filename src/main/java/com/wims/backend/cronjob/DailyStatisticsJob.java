package com.wims.backend.cronjob;

import com.wims.backend.entity.Product;
import com.wims.backend.entity.User;
import com.wims.backend.repository.OrderRepository;
import com.wims.backend.repository.ProcurementRepository;
import com.wims.backend.repository.ProductRepository;
import com.wims.backend.repository.UserRepository;
import com.wims.backend.service.featured.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyStatisticsJob {

    private final OrderRepository orderRepository;
    private final ProcurementRepository procurementRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // Chạy vào lúc 00:00 mỗi ngày
    @Scheduled(cron = "0 0 0 * * ?")
    public void generateDailyStatistics() {
        log.info("Bắt đầu chạy Cronjob: Thống kê & Cảnh báo hàng ngày...");

        try {
            java.time.LocalDateTime startOfDay = java.time.LocalDate.now().atStartOfDay();
            java.time.LocalDateTime endOfDay = java.time.LocalDate.now().atTime(23, 59, 59);

            // 1. Thống kê tổng doanh thu bán ra trong ngày
            BigDecimal dailyRevenue = orderRepository.getDailyRevenue(startOfDay, endOfDay);
            dailyRevenue = dailyRevenue != null ? dailyRevenue : BigDecimal.ZERO;

            // 2. Thống kê tổng tiền nhập hàng trong ngày
            BigDecimal dailyImportAmount = procurementRepository.getDailyImportAmount(startOfDay, endOfDay);
            dailyImportAmount = dailyImportAmount != null ? dailyImportAmount : BigDecimal.ZERO;

            // 3. Quét các sản phẩm sắp hết hàng (< 10)
            List<Product> lowStockProducts = productRepository.findByStockQuantityLessThan(10);

            // 4. Lấy danh sách email của Admin
            List<User> admins = userRepository.findByRoles_Name("ADMIN");
            List<String> adminEmails = admins.stream()
                    .map(User::getEmail)
                    .filter(email -> email != null && !email.isEmpty())
                    .collect(Collectors.toList());

            // 5. Chuẩn bị nội dung Email
            StringBuilder emailBody = new StringBuilder();
            emailBody.append("Kính gửi Ban Quản Trị WIMS,\n\n");
            emailBody.append("Dưới đây là báo cáo thống kê cuối ngày:\n");
            emailBody.append("- Tổng doanh thu bán hàng: ").append(String.format("%,.0f", dailyRevenue)).append(" VNĐ\n");
            emailBody.append("- Tổng chi phí nhập hàng: ").append(String.format("%,.0f", dailyImportAmount)).append(" VNĐ\n\n");
            
            if (!lowStockProducts.isEmpty()) {
                emailBody.append("CẢNH BÁO SẢN PHẨM SẮP HẾT HÀNG:\n");
                for (Product p : lowStockProducts) {
                    emailBody.append(String.format(" - [%s] %s: Còn lại %d sản phẩm\n", p.getCode(), p.getName(), p.getStockQuantity()));
                }
                emailBody.append("\nVui lòng lên kế hoạch nhập hàng kịp thời.\n");
            } else {
                emailBody.append("Tình trạng kho hàng ổn định, không có sản phẩm nào sắp hết.\n");
            }
            
            emailBody.append("\nTrân trọng,\nHệ thống WIMS");

            // 6. Gửi Email
            for (String email : adminEmails) {
                notificationService.sendEmail(email, "[WIMS] Báo cáo thống kê & Cảnh báo tồn kho hàng ngày", emailBody.toString());
            }

            log.info("Đã hoàn thành Cronjob và gửi báo cáo cho {} admin.", adminEmails.size());

        } catch (Exception e) {
            log.error("Lỗi khi chạy Cronjob DailyStatisticsJob: {}", e.getMessage(), e);
        }
    }
}
