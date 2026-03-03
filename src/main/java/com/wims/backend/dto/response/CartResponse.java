package com.wims.backend.dto.response;

import lombok.Builder;
import java.math.BigDecimal;
import java.util.List;

@Builder
public record CartResponse(
        Long id,
        BigDecimal totalAmount,
        List<CartItemResponse> items) {
}
