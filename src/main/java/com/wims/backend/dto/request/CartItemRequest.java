package com.wims.backend.dto.request;

public record CartItemRequest(
        Long productId,
        Integer quantity) {
}
