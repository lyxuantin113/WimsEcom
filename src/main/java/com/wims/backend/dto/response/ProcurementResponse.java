package com.wims.backend.dto.response;

import com.wims.backend.entity.Supplier;
import com.wims.backend.enums.ProcurementStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProcurementResponse {
    private Long id;
    private Supplier supplier;
    private ProcurementStatus status;
    private BigDecimal totalAmount;
    private String note;
    private LocalDateTime approvedAt;
    private String approvedByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private List<ProcurementItemResponse> items;
}
