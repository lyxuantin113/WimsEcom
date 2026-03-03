package com.wims.backend.dto.response;

import com.wims.backend.enums.DiscountScope;
import com.wims.backend.enums.DiscountType;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record DiscountResponse(
        Long id,
        String code,
        String description,
        DiscountType type,
        BigDecimal value,
        DiscountScope scope,
        String applicableIds,
        Integer usageLimit,
        Integer usedCount,
        LocalDateTime startDate,
        LocalDateTime endDate,
        BigDecimal minOrderValue,
        BigDecimal maxDiscountAmount,
        boolean active) {
}