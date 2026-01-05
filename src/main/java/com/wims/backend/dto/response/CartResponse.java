package com.wims.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartResponse {
    private Long id;
    private BigDecimal totalAmount; // Tổng tiền dự tính
    private List<CartItemResponse> items;
}
