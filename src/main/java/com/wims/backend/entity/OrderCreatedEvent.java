package com.wims.backend.entity;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderCreatedEvent extends ApplicationEvent {

    private final Order order;
    private final User user;

    /**
     * @param source : Đối tượng phát ra event (thường là 'this')
     * @param order  : Dữ liệu đơn hàng vừa lưu xong
     * @param user   : Người mua hàng
     */
    public OrderCreatedEvent(Object source, Order order, User user) {
        super(source);
        this.order = order;
        this.user = user;
    }
}
