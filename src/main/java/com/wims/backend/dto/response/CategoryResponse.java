package com.wims.backend.dto.response;

import lombok.Data;

@Data
public class CategoryResponse {
    private Long id;
    private String name;
    // Tuyệt đối KHÔNG đưa List<Product> vào đây để tránh vòng lặp và nặng máy
}