package com.wims.backend.dto.request;

import com.wims.backend.enums.DiscountScope;
import com.wims.backend.enums.DiscountType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DiscountRequest(
        String code,
        String description,
        DiscountType type,
        BigDecimal value,
        DiscountScope scope,
        String applicableIds,
        Integer usageLimit,
        LocalDateTime startDate,
        LocalDateTime endDate,
        BigDecimal minOrderValue,
        BigDecimal maxDiscountAmount,
        boolean active) {
}