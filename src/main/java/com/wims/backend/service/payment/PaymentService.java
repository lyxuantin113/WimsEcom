package com.wims.backend.service.payment;

import com.wims.backend.dto.ApiResponse;
import com.wims.backend.entity.Order;
import com.wims.backend.entity.User;
import com.wims.backend.enums.OrderStatus;
import com.wims.backend.exception.AppException;
import com.wims.backend.repository.OrderRepository;
import com.wims.backend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentFactory paymentFactory;
    private final OrderRepository orderRepository;
    private final SecurityUtils securityUtils;

    /**
     * Tạo URL thanh toán dựa trên phương thức được chọn
     */
    public String createPaymentUrl(String method, long orderId, String ipAddress) {
        // 1. Lấy user hiện tại
        User user = securityUtils.getCurrentUserLogin();

        // 2. Tìm đơn hàng
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(1004, "Đơn hàng không tồn tại"));

        // 3. Kiểm tra quyền sở hữu
        if (!order.getUser().getUsername().equals(user.getUsername())) {
            throw new AppException(403, "Bạn không có quyền thanh toán đơn hàng này");
        }

        // 4. Kiểm tra trạng thái đơn hàng (Chỉ cho phép thanh toán nếu đang chờ)
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT &&
                order.getStatus() != OrderStatus.PENDING_CONFIRMATION) {
            throw new AppException(1009, "Đơn hàng không ở trạng thái chờ thanh toán");
        }

        // 5. Lấy chiến lược và tạo URL
        return paymentFactory.getStrategy(method).createPaymentUrl(order, ipAddress);
    }

    /**
     * Xử lý kết quả trả về từ cổng thanh toán
     */
    public ApiResponse<?> handleCallback(String method, Map<String, String> params) {
        return paymentFactory.getStrategy(method).handleCallback(params);
    }
}