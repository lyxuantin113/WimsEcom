package com.wims.backend.controller;

import com.wims.backend.dto.ApiResponse;
import com.wims.backend.dto.request.ProductRequestDTO;
import com.wims.backend.dto.response.PageResponse;
import com.wims.backend.dto.response.ProductResponse;
import com.wims.backend.entity.Product;
import com.wims.backend.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController // Đánh dấu đây là API Controller
@RequestMapping("/api/products") // Đường dẫn gốc: http://localhost:8080/api/products
public class ProductController {

    @Autowired
    private ProductService productService;

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
            @RequestParam(required = false) Long categoryId
    ) {
        return ApiResponse.<PageResponse<ProductResponse>>builder()
                .result(productService.getAllProducts(page, size, sortBy, keyword, minPrice, maxPrice, isOutOfStock, categoryId))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getProduct(@PathVariable Long id) {
        return ApiResponse.<ProductResponse>builder().result(productService.getProductById(id)).build();
    }

    // API: Tạo mới (POST)
    // consumes: Báo hiệu API này nhận Form Data (bao gồm file)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProductResponse> create(
            @ModelAttribute @Valid ProductRequestDTO request
    ) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.createProduct(request))
                .build();
    }

    // API: Sửa thông tin (PUT)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductResponse> update(@PathVariable Long id, @ModelAttribute @Valid ProductRequestDTO request) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.updateProduct(id, request))
                .build();
    }

    // API: Xóa (DELETE)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ApiResponse.<String>builder()
                .result("Đã xóa thành công sản phẩm")
                .build();
    }

    // Get Related Product
    @GetMapping("/{id}/related")
    public ApiResponse<List<ProductResponse>> getRelatedProducts(@PathVariable Long id) {
        return ApiResponse.<List<ProductResponse>>builder()
                .result(productService.getRelatedProducts(id))
                .build();
    }
}