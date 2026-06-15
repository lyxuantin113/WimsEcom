package com.wims.backend.listener;

import com.wims.backend.entity.Order;
import com.wims.backend.enums.OrderStatus;
import com.wims.backend.event.OrderCreatedEvent;
import com.wims.backend.event.OrderStatusChangedEvent;
import com.wims.backend.service.featured.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderNotificationListener {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) {
        Order order = event.getOrder();
        log.info("Processing ORDER_CREATED event for order #{}", order.getId());

        String subject = "Xác nhận đơn hàng #" + order.getId();
        String body = "Cảm ơn " + order.getCustomerName() + " đã mua hàng tại WIMS.";

        notificationService.sendEmail(event.getUser().getEmail(), subject, body);
        notificationService.sendWebSocketNotification(event.getUser().getUsername(), "Có đơn hàng mới!", order.getId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStatusChange(OrderStatusChangedEvent event) {
        Order order = event.getOrder();
        log.info("Processing STATUS_CHANGED event for order #{}", order.getId());

        String statusVN = getStatusInVietnamese(event.getNewStatus());
        String notiMsg = "Đơn hàng #" + order.getId() + " đã chuyển sang trạng thái: " + statusVN;

        // 1. Socket (Luôn gửi)
        notificationService.sendWebSocketNotification(order.getUser().getUsername(), notiMsg, order.getId());

        // 2. Email (Có điều kiện)
        if (shouldSendEmail(event.getNewStatus())) {
            String subject = "Cập nhật đơn hàng #" + order.getId();
            String body = "Xin chào " + order.getCustomerName() + ",\n\n"
                    + notiMsg + "\nCảm ơn bạn đã mua hàng tại WIMS.";

            notificationService.sendEmail(order.getUser().getEmail(), subject, body);
        }
    }

    private boolean shouldSendEmail(OrderStatus status) {
        return status == OrderStatus.PAID
                || status == OrderStatus.SHIPPING
                || status == OrderStatus.COMPLETED
                || status == OrderStatus.CANCELLED;
    }

    private String getStatusInVietnamese(OrderStatus status) {
        if (status == null)
            return "Trạng thái không xác định";
        return switch (status) {
            case PENDING_PAYMENT -> "Chờ thanh toán";
            case PENDING_CONFIRMATION -> "Chờ xác nhận";
            case PAID -> "Đã thanh toán";
            case CONFIRMED -> "Đã xác nhận";
            case SHIPPING -> "Đang giao hàng";
            case COMPLETED -> "Giao hàng thành công";
            case CANCELLED -> "Đã hủy";
            case RETURN_REQUESTED -> "Yêu cầu trả hàng";
            case RETURNED -> "Đã trả hàng";
            default -> status.name();
        };
    }
}
