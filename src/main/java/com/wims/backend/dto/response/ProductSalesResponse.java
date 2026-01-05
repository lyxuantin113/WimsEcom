package com.wims.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSalesResponse {
    private Long id;
    private String name;
    private String image;
    private Long totalSold;     // Tổng số lượng bán
    private BigDecimal revenue; // Tổng tiền thu được từ SP này
}
