package com.wims.backend.dto.request;

import com.wims.backend.enums.DiscountScope;
import com.wims.backend.enums.DiscountType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DiscountRequest {
    private String code;
    private String description;
    private DiscountType type; // PERCENTAGE hoặc FIXED_AMOUNT
    private BigDecimal value; // Giá trị giảm
    private DiscountScope scope; // GLOBAL, SPECIFIC_PRODUCT, ...
    private String applicableIds; // "1,2,3"
    private Integer usageLimit;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscountAmount;
    private boolean active;
}