package com.wims.backend.dto.response;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record OrderDetailResponse(
        Long id,
        Long productId,
        String productName,
        String productImage,
        Integer quantity,
        BigDecimal price,
        boolean isDiscounted) {
}
