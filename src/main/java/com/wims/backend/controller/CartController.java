package com.wims.backend.controller;

import com.wims.backend.dto.ApiResponse;
import com.wims.backend.dto.request.CartItemRequest;
import com.wims.backend.dto.response.CartResponse;
import com.wims.backend.entity.User;
import com.wims.backend.exception.AppException;
import com.wims.backend.security.CustomUserDetails;
import com.wims.backend.service.based.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // 1. Xem giỏ hàng của tôi
    @GetMapping
    public ApiResponse<CartResponse> getMyCart() {


        return ApiResponse.<CartResponse>builder()
                .result(cartService.getMyCart())
                .build();
    }

    // 2. Thêm sản phẩm vào giỏ
    @PostMapping
    public ApiResponse<CartResponse> addToCart(
            @RequestBody @Valid CartItemRequest request
    ) {
        return ApiResponse.<CartResponse>builder()
                .result(cartService.addToCart(request))
                .build();
    }

    // Trong CartController.java

    @PutMapping("/items/{itemId}")
    public ApiResponse<CartResponse> updateItem(
            @PathVariable Long itemId,
            @RequestBody CartItemRequest request
    ) {
        // request.getQuantity() chứa số lượng mới
        return ApiResponse.<CartResponse>builder()
                .result(cartService.updateCartItem(itemId, request.getQuantity()))
                .build();
    }

    @DeleteMapping("/items/{itemId}")
    public ApiResponse<CartResponse> deleteItem(@PathVariable Long itemId) {
        return ApiResponse.<CartResponse>builder()
                .result(cartService.removeCartItem(itemId))
                .build();
    }
}