package com.wims.backend.controller;

import com.wims.backend.configuration.VNPayConfig;
import com.wims.backend.dto.ApiResponse;
import com.wims.backend.dto.VNPayResponse;
import com.wims.backend.service.feature.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final VNPayService vnPayService;

    // API tạo link thanh toán
    @GetMapping("/vnpay")
    public ApiResponse<String> createPayment(
            @RequestParam long orderId,
            HttpServletRequest request
    ) {
        // Gọi service
        String vnpayUrl = vnPayService.createPaymentUrl(orderId, VNPayConfig.getIpAddress(request));

        return ApiResponse.<String>builder()
                .result(vnpayUrl)
                .build();
    }

    @GetMapping("/vnpay-callback")
    public ApiResponse<VNPayResponse> paymentCallback(HttpServletRequest request) {
        // Gọi Service xử lý
        return vnPayService.handleCallback(request);
    }
}