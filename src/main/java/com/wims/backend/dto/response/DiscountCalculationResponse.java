package com.wims.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DiscountCalculationResponse {
    private BigDecimal totalDiscount;       // Tổng tiền được giảm
    private List<Long> affectedProductIds;  // Danh sách ID các sản phẩm được hưởng khuyến mãi
}