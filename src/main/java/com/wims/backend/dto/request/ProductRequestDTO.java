package com.wims.backend.dto.request;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

public record ProductRequestDTO(
        @NotBlank(message = "Mã sản phẩm không được để trống") @Size(min = 3, max = 10, message = "Mã sản phẩm phải từ 3 đến 10 ký tự") String code,

        @NotBlank(message = "Tên sản phẩm không được để trống") String name,

        String description,

        @NotNull(message = "Giá tiền không được để trống") @Min(value = 0, message = "Giá tiền không được âm") BigDecimal price,

        @NotNull(message = "Số lượng không được để trống") @Min(value = 0, message = "Số lượng tồn kho phải lớn hơn hoặc bằng 0") Integer stockQuantity,

        MultipartFile file,

        @NotNull(message = "Danh mục không được để trống") Long categoryId) {
}