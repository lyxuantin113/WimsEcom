package com.wims.backend.dto.response;

import lombok.Builder;
import java.math.BigDecimal;
import java.util.List;

@Builder
public record DiscountCalculationResponse(
        BigDecimal totalDiscount,
        List<Long> affectedProductIds) {
}