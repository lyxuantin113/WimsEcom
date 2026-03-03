package com.wims.backend.dto.request;

import java.util.List;

public record DiscountCalculationRequest(
        String code,
        List<CartItemRequest> items) {
}