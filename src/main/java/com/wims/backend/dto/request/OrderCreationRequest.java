package com.wims.backend.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public record OrderCreationRequest(
                @NotBlank(message = "Customer name is required") String customerName,
                @NotBlank(message = "Phone is required") String phone,
                @NotBlank(message = "Address is required") String address,
                @NotBlank(message = "Payment method is required") String paymentMethod,
                List<CartItemRequest> items,
                String discountCode) {
}
