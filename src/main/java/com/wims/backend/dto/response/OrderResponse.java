package com.wims.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;

    // Thay vì User entity, ta dùng UserResponse
    private UserResponse user;

    private String customerName;
    private String phone;
    private String address;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private String status;
    private LocalDateTime createdAt;
    private String discountCode;
    // List chi tiết đơn hàng
    private List<OrderDetailResponse> orderDetails;
}
