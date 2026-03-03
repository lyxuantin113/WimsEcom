package com.wims.backend.dto.response;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record ProductSalesResponse(
        Long id,
        String name,
        String image,
        Long totalSold,
        BigDecimal revenue) {
}
