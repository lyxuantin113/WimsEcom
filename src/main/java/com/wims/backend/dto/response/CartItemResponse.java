package com.wims.backend.dto.response;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        String productImage,
        BigDecimal price,
        Integer quantity,
        BigDecimal totalPrice) {
}
