package com.wims.backend.dto.request;

import java.util.List;

public record OrderCreationRequest(
        String customerName,
        String phone,
        String address,
        String paymentMethod,
        List<CartItemRequest> items,
        String discountCode) {
}
