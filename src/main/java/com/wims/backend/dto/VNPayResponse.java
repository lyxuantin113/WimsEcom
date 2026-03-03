package com.wims.backend.dto;

import lombok.Builder;

@Builder
public record VNPayResponse(
        String transactionId,
        String orderId,
        String paymentTime,
        String totalPrice) {
}
