package com.wims.backend.dto.response;

import com.wims.backend.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InventoryTransactionResponse {
    private Long id;
    private Long productId;
    private String productCode;
    private String productName;
    private Integer quantity;
    private TransactionType transactionType;
    private Long referenceId;
    private String note;
    private LocalDateTime createdAt;
}
