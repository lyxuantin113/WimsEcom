package com.wims.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequestDTO(
        @NotBlank(message = "Tên danh mục không được để trống") String name) {
}