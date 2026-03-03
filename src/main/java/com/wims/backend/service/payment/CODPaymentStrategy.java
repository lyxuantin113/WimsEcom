package com.wims.backend.service.payment;

import com.wims.backend.dto.ApiResponse;
import com.wims.backend.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CODPaymentStrategy implements PaymentStrategy {

    @Override
    public String createPaymentUrl(Order order, String ipAddress) {
        // COD doesn't need a payment URL, just return a confirmation message or null
        return "COD_SUCCESS";
    }

    @Override
    public ApiResponse<?> handleCallback(Map<String, String> params) {
        // COD usually doesn't have a callback in this sense, but we can implement it if
        // needed
        return ApiResponse.success("COD payment accepted").build();
    }

    @Override
    public String getMethodName() {
        return "COD";
    }
}
