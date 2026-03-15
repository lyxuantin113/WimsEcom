package com.wims.backend.controller;

import com.wims.backend.dto.request.OrderCreationRequest;
import com.wims.backend.dto.ApiResponse;
import com.wims.backend.dto.response.OrderResponse;
import com.wims.backend.dto.response.PageResponse;
import com.wims.backend.service.based.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

        private final OrderService orderService;

        @PostMapping
        public ApiResponse<OrderResponse> createOrder(@RequestBody OrderCreationRequest request) {
                return ApiResponse.success(orderService.createOrder(request)).build();
        }

        @GetMapping("/my-orders")
        public ApiResponse<PageResponse<OrderResponse>> getMyOrders(
                        @RequestParam(defaultValue = "1", required = false) int page,
                        @RequestParam(defaultValue = "10", required = false) int size,
                        @RequestParam(defaultValue = "createdAt", required = false) String sortBy) {
                return ApiResponse.success(orderService.getMyOrders(page, size, sortBy)).build();
        }

        @GetMapping
        @PreAuthorize("hasRole('ADMIN')")
        public ApiResponse<PageResponse<OrderResponse>> getAllOrders(
                        @RequestParam(defaultValue = "1", required = false) int page,
                        @RequestParam(defaultValue = "10", required = false) int size,
                        @RequestParam(defaultValue = "createdAt", required = false) String sortBy) {
                return ApiResponse.success(orderService.getAllOrders(page, size, sortBy)).build();
        }

        @PutMapping("/{id}/status")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiResponse<OrderResponse> updateStatus(
                        @PathVariable Long id,
                        @RequestParam String status) {
                return ApiResponse.success(orderService.updateOrderStatus(id, status)).build();
        }

        @GetMapping("/{id}")
        public ApiResponse<OrderResponse> getOrderById(@PathVariable Long id) {
                return ApiResponse.success(orderService.getOrderById(id)).build();
        }

        @PutMapping("/{id}/cancel")
        public ApiResponse<OrderResponse> cancelOrder(@PathVariable Long id) {
                return ApiResponse.success(orderService.cancelOrder(id)).build();
        }

        @PutMapping("/{id}/return")
        public ApiResponse<OrderResponse> requestReturn(@PathVariable Long id) {
                return ApiResponse.success(orderService.requestReturn(id)).build();
        }

}