package com.wims.backend.listener;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.wims.backend.dto.OrderNotificationMessage;
import com.wims.backend.entity.Order;
import com.wims.backend.event.OrderCreatedEvent;
import com.wims.backend.event.OrderStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderNotificationListener {

    private final PubSubTemplate pubSubTemplate;
    private static final String TOPIC_NAME = "order-notifications";

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) {
        Order order = event.getOrder();

        OrderNotificationMessage message = OrderNotificationMessage.builder()
                .orderId(order.getId())
                .username(event.getUser().getUsername())
                .email(event.getUser().getEmail())
                .customerName(order.getCustomerName())
                .type(OrderNotificationMessage.NotificationType.ORDER_CREATED)
                .build();

        log.info("Publishing ORDER_CREATED event for order #{} to Pub/Sub", order.getId());
        pubSubTemplate.publish(TOPIC_NAME, message);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStatusChange(OrderStatusChangedEvent event) {
        Order order = event.getOrder();

        OrderNotificationMessage message = OrderNotificationMessage.builder()
                .orderId(order.getId())
                .username(order.getUser().getUsername())
                .email(order.getUser().getEmail())
                .customerName(order.getCustomerName())
                .status(event.getNewStatus())
                .type(OrderNotificationMessage.NotificationType.STATUS_CHANGED)
                .build();

        log.info("Publishing STATUS_CHANGED event for order #{} to Pub/Sub", order.getId());
        pubSubTemplate.publish(TOPIC_NAME, message);
    }
}
