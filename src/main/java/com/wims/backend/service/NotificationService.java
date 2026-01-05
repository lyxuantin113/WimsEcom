package com.wims.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j // Để ghi log lỗi nếu gửi mail thất bại
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate; // Của WebSocket
    private final JavaMailSender mailSender; // Của Email

    /**
     * Gửi thông báo Real-time qua WebSocket
     */
    public void sendWebSocketNotification(String username, String message, Long orderId) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("message", message);
            payload.put("orderId", orderId);
            payload.put("timestamp", System.currentTimeMillis());

            // Gửi đến topic riêng của user: /topic/notifications/{userId}
            String destination = "/topic/notifications/" + username;
            messagingTemplate.convertAndSend(destination, payload);

            log.info("Đã gửi socket đến user {}: {}", username, message);
        } catch (Exception e) {
            log.error("Lỗi gửi WebSocket: ", e);
        }
    }

    /**
     * Gửi Email đơn giản (Text only)
     */
    public void sendEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("WIMS E-commerce <no-reply@wims.com>");
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Đã gửi email đến {}", toEmail);
        } catch (Exception e) {
            log.error("Lỗi gửi Email đến {}: {}", toEmail, e.getMessage());
            // Không throw exception để tránh làm rollback transaction của đơn hàng
        }
    }
}