package com.wims.backend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ProcurementRequest(
        @NotNull(message = "Supplier ID không được để trống")
        Long supplierId,
        
        String note,

        @NotEmpty(message = "Danh sách sản phẩm không được rỗng")
        List<ProcurementItemRequest> items
) {}
