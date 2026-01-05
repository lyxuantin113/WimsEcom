package com.wims.backend.dto.request;

import jakarta.validation.constraints.*; // Import bộ validation
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
public class ProductRequestDTO {

    @NotBlank(message = "Mã sản phẩm không được để trống") // Cấm null và cấm chuỗi rỗng "" hoặc "   "
    @Size(min = 3, max = 10, message = "Mã sản phẩm phải từ 3 đến 10 ký tự")
    private String code;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    private String description;

    @NotNull(message = "Giá tiền không được để trống")
    @Min(value = 0, message = "Giá tiền không được âm") // Giá >= 0
    private BigDecimal price;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng tồn kho phải lớn hơn hoặc bằng 0")
    private Integer stockQuantity;

    private MultipartFile file;

    // Relations
    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;
}