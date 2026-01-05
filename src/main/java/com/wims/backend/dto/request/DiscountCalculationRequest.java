package com.wims.backend.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class DiscountCalculationRequest {
    private String code;
    private List<CartItemRequest> items; // Danh sách sản phẩm để check scope
}