package com.wims.backend.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductResponse {
    // Đây là những gì Frontend ĐƯỢC PHÉP nhìn thấy
    private Long id;
    private String code;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;

    private String image;
    // Repository
    private String categoryName;

}