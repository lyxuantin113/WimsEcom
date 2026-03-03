package com.wims.backend.controller;

import com.wims.backend.dto.ApiResponse;
import com.wims.backend.dto.request.ProductRequestDTO;
import com.wims.backend.dto.response.PageResponse;
import com.wims.backend.dto.response.ProductResponse;
import com.wims.backend.entity.User;
import com.wims.backend.service.based.ProductService;
import com.wims.backend.service.featured.SearchHistoryService;
import com.wims.backend.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final SearchHistoryService searchHistoryService;

    private final SecurityUtils securityUtils;

    // API: Lấy danh sách (GET)
    @GetMapping
    public ApiResponse<PageResponse<ProductResponse>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            // Các tham số tìm kiếm (required = false nghĩa là không bắt buộc phải có)
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) boolean isOutOfStock,
            @RequestParam(required = false) Long categoryId) {
        User user = securityUtils.getCurrentUserLogin();

        if (keyword != null && !keyword.trim().isEmpty() && user != null) {
            // Con phải gọi service để lưu vào Redis List
            searchHistoryService.saveSearchHistory(user.getId(), keyword.trim());
        }

        var result = productService.getAllProducts(page, size, sortBy, keyword, minPrice, maxPrice, isOutOfStock,
                categoryId);

        return ApiResponse.success(result).build();
    }

    @GetMapping("/search-history")
    public ApiResponse<List<String>> getSearchHistory() {
        User user = securityUtils.getCurrentUserLogin();

        if (user == null) {
            return ApiResponse.success(Collections.<String>emptyList()).build();
        }

        Long userId = user.getId();

        // Nếu đã login, mới lấy ID
        return ApiResponse.success(searchHistoryService.getSearchHistory(userId)).build();
    }

    @DeleteMapping("/search-history")
    public ApiResponse<Void> deleteSearchHistory(
            @RequestParam String keyword) {
        User user = securityUtils.getCurrentUserLogin();

        if (user != null) {
            // Gọi hàm remove trong RedisService (Con cần viết thêm hàm này nếu chưa có)
            searchHistoryService.removeKeyword(user.getId(), keyword);
        }
        return ApiResponse.<Void>builder()
                .message("Đã xóa từ khóa khỏi lịch sử")
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getProduct(@PathVariable Long id) {
        return ApiResponse.success(productService.getProductById(id)).build();
    }

    // API: Tạo mới (POST)
    // consumes: Báo hiệu API này nhận Form Data (bao gồm file)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProductResponse> create(
            @ModelAttribute @Valid ProductRequestDTO request) {
        return ApiResponse.success(productService.createProduct(request)).build();
    }

    // API: Sửa thông tin (PUT)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductResponse> update(@PathVariable Long id,
            @ModelAttribute @Valid ProductRequestDTO request) {
        return ApiResponse.success(productService.updateProduct(id, request)).build();
    }

    // API: Xóa (DELETE)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ApiResponse.success("Đã xóa thành công sản phẩm").build();
    }

    // Get Related Product
    @GetMapping("/{id}/related")
    public ApiResponse<List<ProductResponse>> getRelatedProducts(@PathVariable Long id) {
        return ApiResponse.success(productService.getRelatedProducts(id)).build();
    }
}