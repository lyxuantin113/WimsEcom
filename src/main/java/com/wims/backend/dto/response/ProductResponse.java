package com.wims.backend.dto.response;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record ProductResponse(
        Long id,
        String code,
        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        String image,
        String categoryName) {
}