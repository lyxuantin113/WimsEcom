package com.wims.backend.service.payment;

import com.wims.backend.entity.Order;
import com.wims.backend.dto.ApiResponse;

public interface PaymentStrategy {
    String createPaymentUrl(Order order, String ipAddress);

    ApiResponse<?> handleCallback(java.util.Map<String, String> params);

    String getMethodName();
}
