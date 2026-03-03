package com.wims.backend.dto.request;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

public record ProductRequestDTO(
                @NotBlank(message = "Product code is required") @Size(min = 3, max = 10, message = "Product code must be between 3 and 10 characters") String code,

                @NotBlank(message = "Product name is required") String name,

                String description,

                @NotNull(message = "Price is required") @Min(value = 0, message = "Price cannot be negative") BigDecimal price,

                @NotNull(message = "Stock quantity is required") @Min(value = 0, message = "Stock quantity must be greater than or equal to 0") Integer stockQuantity,

                MultipartFile file,

                @NotNull(message = "Category is required") Long categoryId) {
}