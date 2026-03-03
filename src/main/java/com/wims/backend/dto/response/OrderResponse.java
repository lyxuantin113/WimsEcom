package com.wims.backend.dto.response;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderResponse(
        Long id,
        UserResponse user,
        String customerName,
        String phone,
        String address,
        BigDecimal totalAmount,
        BigDecimal discountAmount,
        String status,
        LocalDateTime createdAt,
        String discountCode,
        List<OrderDetailResponse> orderDetails) {
}
