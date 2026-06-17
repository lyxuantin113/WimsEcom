package com.wims.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ProcurementItemRequest(
        @NotNull(message = "Product ID không được để trống")
        Long productId,

        @NotNull(message = "Số lượng không được để trống")
        @Min(value = 1, message = "Số lượng phải lớn hơn 0")
        Integer quantity,

        @NotNull(message = "Giá nhập không được để trống")
        @Min(value = 0, message = "Giá nhập không được âm")
        BigDecimal unitPrice
) {}
