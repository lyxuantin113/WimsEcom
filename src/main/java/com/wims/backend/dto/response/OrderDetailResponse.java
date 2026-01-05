package com.wims.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderDetailResponse {
    private Long id;
    private Long productId;
    private String productName; // Lấy tên SP ra luôn cho tiện hiển thị
    private String productImage; // Lấy ảnh ra luôn
    private Integer quantity;
    private BigDecimal price;

    private Boolean isDiscounted;
}
