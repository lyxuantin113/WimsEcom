package com.wims.backend.controller;

import com.wims.backend.dto.ApiResponse;
import com.wims.backend.dto.request.DiscountCalculationRequest;
import com.wims.backend.dto.request.DiscountRequest;
import com.wims.backend.dto.response.DiscountCalculationResponse;
import com.wims.backend.dto.response.DiscountResponse;
import com.wims.backend.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
public class DiscountController {
    private final DiscountService discountService;

    // API này KHÔNG cần login cũng gọi được (để khách vãng lai check giá)
    // Nhớ cấu hình permitAll() trong SecurityConfig
    @PostMapping("/calculate")
    public ApiResponse<DiscountCalculationResponse> calculateDiscount(@RequestBody DiscountCalculationRequest request) {
        DiscountCalculationResponse discountCalculationRes = discountService.calculateDiscount(request);
        return ApiResponse.<DiscountCalculationResponse>builder()
                .result(discountCalculationRes)
                .build();
    }

    // 1. Lấy danh sách
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')") // Bật lên nếu đã cấu hình security
    public ApiResponse<List<DiscountResponse>> getAllDiscounts() {
        return ApiResponse.<List<DiscountResponse>>builder()
                .result(discountService.getAllDiscounts())
                .build();
    }

    // 2. Tạo mới
    @PostMapping
    public ApiResponse<DiscountResponse> createDiscount(@RequestBody DiscountRequest request) {
        return ApiResponse.<DiscountResponse>builder()
                .result(discountService.createDiscount(request))
                .build();
    }

    // 3. Cập nhật
    @PutMapping("/{id}")
    public ApiResponse<DiscountResponse> updateDiscount(@PathVariable Long id, @RequestBody DiscountRequest request) {
        return ApiResponse.<DiscountResponse>builder()
                .result(discountService.updateDiscount(id, request))
                .build();
    }

    // 4. Xóa
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteDiscount(@PathVariable Long id) {
        discountService.deleteDiscount(id);
        return ApiResponse.<String>builder()
                .result("Đã xóa mã giảm giá thành công")
                .build();
    }
}