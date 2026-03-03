package com.wims.backend.controller;

import com.wims.backend.dto.ApiResponse;
import com.wims.backend.dto.request.CartItemRequest;
import com.wims.backend.dto.response.CartResponse;
import com.wims.backend.service.based.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

        private final CartService cartService;

        // 1. Xem giỏ hàng của tôi
        @GetMapping
        public ApiResponse<CartResponse> getMyCart() {
                return ApiResponse.success(cartService.getMyCart()).build();
        }

        @PostMapping
        public ApiResponse<CartResponse> addToCart(
                        @RequestBody @Valid CartItemRequest request) {
                return ApiResponse.success(cartService.addToCart(request)).build();
        }

        // Trong CartController.java

        @PutMapping("/items/{itemId}")
        public ApiResponse<CartResponse> updateItem(
                        @PathVariable Long itemId,
                        @RequestBody CartItemRequest request) {
                return ApiResponse.success(cartService.updateCartItem(itemId, request.quantity())).build();
        }

        @DeleteMapping("/items/{itemId}")
        public ApiResponse<CartResponse> deleteItem(@PathVariable Long itemId) {
                return ApiResponse.success(cartService.removeCartItem(itemId)).build();
        }
}