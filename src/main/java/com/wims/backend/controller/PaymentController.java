package com.wims.backend.controller;

import com.wims.backend.configuration.VNPayConfig;
import com.wims.backend.dto.ApiResponse;
import com.wims.backend.dto.VNPayResponse;
import com.wims.backend.service.payment.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    // API tạo link thanh toán (VNPAY là mặc định cho endpoint này)
    @GetMapping("/vnpay")
    public ApiResponse<String> createPayment(
            @RequestParam long orderId,
            HttpServletRequest request) {
        // Gọi service qua factory (strategy: VNPAY)
        String vnpayUrl = paymentService.createPaymentUrl("VNPAY", orderId, VNPayConfig.getIpAddress(request));

        return ApiResponse.success(vnpayUrl).build();
    }

    @GetMapping("/vnpay-callback")
    public ApiResponse<?> paymentCallback(HttpServletRequest request) {
        // Chuyển Request parameter map thành Map<String, String> để gửi qua PaymentStrategy
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, value) -> {
            if (value != null && value.length > 0) {
                params.put(key, value[0]);
            }
        });
        
        // Gọi Service xử lý qua factory (strategy: VNPAY)
        return paymentService.handleCallback("VNPAY", params);
    }
}