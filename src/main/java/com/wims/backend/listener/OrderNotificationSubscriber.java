package com.wims.backend.listener;

import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import com.wims.backend.dto.OrderNotificationMessage;
import com.wims.backend.enums.OrderStatus;
import com.wims.backend.service.featured.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderNotificationSubscriber {

    private final NotificationService notificationService;

    @ServiceActivator(inputChannel = "orderNotificationInputChannel")
    public void receiveMessage(OrderNotificationMessage message,
            @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage ackMessage) {

        log.info("Received message from Pub/Sub: Order #{} Type: {}", message.getOrderId(), message.getType());

        try {
            if (message.getType() == OrderNotificationMessage.NotificationType.ORDER_CREATED) {
                handleOrderCreated(message);
            } else if (message.getType() == OrderNotificationMessage.NotificationType.STATUS_CHANGED) {
                handleStatusChanged(message);
            }

            // Xác nhận đã xử lý xong tin nhắn
            ackMessage.ack();
        } catch (Exception e) {
            log.error("Error processing Pub/Sub message: ", e);
            // Nack để Pub/Sub gửi lại tin nhắn sau (nếu cần)
            ackMessage.nack();
        }
    }

    private void handleOrderCreated(OrderNotificationMessage message) {
        String subject = "Xác nhận đơn hàng #" + message.getOrderId();
        String body = "Cảm ơn " + message.getCustomerName() + " đã mua hàng tại WIMS.";

        notificationService.sendEmail(message.getEmail(), subject, body);
        notificationService.sendWebSocketNotification(message.getUsername(), "Có đơn hàng mới!", message.getOrderId());
    }

    private void handleStatusChanged(OrderNotificationMessage message) {
        String statusVN = getStatusInVietnamese(message.getStatus());
        String notiMsg = "Đơn hàng #" + message.getOrderId() + " đã chuyển sang trạng thái: " + statusVN;

        // 1. Socket (Luôn gửi)
        notificationService.sendWebSocketNotification(message.getUsername(), notiMsg, message.getOrderId());

        // 2. Email (Có điều kiện)
        if (shouldSendEmail(message.getStatus())) {
            String subject = "Cập nhật đơn hàng #" + message.getOrderId();
            String body = "Xin chào " + message.getCustomerName() + ",\n\n"
                    + notiMsg + "\nCảm ơn bạn đã mua hàng tại WIMS.";

            notificationService.sendEmail(message.getEmail(), subject, body);
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
