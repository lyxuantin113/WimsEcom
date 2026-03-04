package com.wims.backend.dto;

import com.wims.backend.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderNotificationMessage implements Serializable {
    private Long orderId;
    private String username;
    private String email;
    private String customerName;
    private String message;
    private OrderStatus status;
    private NotificationType type;

    public enum NotificationType {
        ORDER_CREATED,
        STATUS_CHANGED
    }
}
