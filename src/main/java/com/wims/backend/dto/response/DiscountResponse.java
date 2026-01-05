package com.wims.backend.dto.response;

import com.wims.backend.enums.DiscountScope;
import com.wims.backend.enums.DiscountType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class DiscountResponse {
    private Long id;
    private String code;
    private String description;
    private DiscountType type;
    private BigDecimal value;
    private DiscountScope scope;
    private String applicableIds;
    private Integer usageLimit;
    private Integer usedCount; // Quan trọng để hiện số lượt đã dùng
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscountAmount;
    private boolean active;
}