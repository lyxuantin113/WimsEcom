package com.wims.backend.listener;

import com.wims.backend.entity.Order;
import com.wims.backend.enums.OrderStatus;
import com.wims.backend.event.OrderCreatedEvent;
import com.wims.backend.entity.User;
import com.wims.backend.event.OrderStatusChangedEvent;
import com.wims.backend.service.featured.NotificationService;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
// import các service và event cần thiết

@Component // Đánh dấu là Bean để Spring quản lý
@RequiredArgsConstructor
public class OrderNotificationListener {

    private final NotificationService notificationService; // Inject công cụ gửi mail

    // Ông này đóng vai trò "Điều phối viên"
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleOrderCreated(OrderCreatedEvent event) {
        // 1. Lấy dữ liệu từ Event
        User user = event.getUser();
        Order order = event.getOrder();

        // 2. Soạn nội dung (Logic nghiệp vụ nằm ở đây)
        String subject = "Xác nhận đơn hàng #" + order.getId();
        String body = "Cảm ơn " + user.getFullName() + " đã mua hàng...";

        // 3. Gọi công cụ để gửi
        notificationService.sendEmail(user.getEmail(), subject, body);
        notificationService.sendWebSocketNotification(user.getUsername(), "Có đơn mới!", order.getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleStatusChange(OrderStatusChangedEvent event) {
        Order order = event.getOrder();
        OrderStatus newStatus = event.getNewStatus();

        String email = order.getUser().getEmail();
        String username = order.getUser().getUsername();

        String newStatusVN = getStatusInVietnamese(newStatus);
        String notiMsg = "Đơn hàng #" + order.getId() + " đã chuyển sang trạng thái: " + newStatusVN;

        // 1. Socket (Luôn gửi)
        notificationService.sendWebSocketNotification(username, notiMsg, order.getId());

        // 2. Email (Có điều kiện)
        if (shouldSendEmail(newStatus)) {
            String subject = "Cập nhật đơn hàng #" + order.getId();
            String body = "Xin chào " + order.getCustomerName() + ",\n\n"
                    + notiMsg + "\nCảm ơn bạn đã mua hàng tại WIMS.";

            notificationService.sendEmail(email, subject, body);
        }
    }

    // Check trạng thái gửi mail
    private boolean shouldSendEmail(OrderStatus status) {
        return status == OrderStatus.PAID
                || status == OrderStatus.SHIPPING
                || status == OrderStatus.COMPLETED
                || status == OrderStatus.CANCELLED;
    }

    private String getStatusInVietnamese(OrderStatus status) {
        if (status == null)
            return "Trạng thái không xác định";

        switch (status) {
            case PENDING_PAYMENT:
                return "Chờ thanh toán";
            case PENDING_CONFIRMATION:
                return "Chờ xác nhận";
            case PAID:
                return "Đã thanh toán";
            case CONFIRMED:
                return "Đã xác nhận";
            case SHIPPING:
                return "Đang giao hàng";
            case COMPLETED:
                return "Giao hàng thành công";
            case CANCELLED:
                return "Đã hủy";
            case RETURN_REQUESTED:
                return "Yêu cầu trả hàng";
            case RETURNED:
                return "Đã trả hàng";
            default:
                return status.name(); // Trường hợp lạ thì trả về tiếng Anh gốc
        }
    }
}