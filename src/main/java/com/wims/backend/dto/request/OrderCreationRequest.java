package com.wims.backend.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class OrderCreationRequest {
    private String customerName;
    private String phone;
    private String address;
    private String paymentMethod;
    // Danh sách các món muốn mua
    private List<CartItemRequest> items;
    private String discountCode;
}
