package com.wims.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class VNPayResponse {
    private String transactionId;
    private String orderId;
    private String paymentTime;
    private String totalPrice;
}
