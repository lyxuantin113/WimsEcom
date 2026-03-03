package com.wims.backend.dto.response;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record MonthlyRevenue(
        int month,
        BigDecimal revenue) {
}
