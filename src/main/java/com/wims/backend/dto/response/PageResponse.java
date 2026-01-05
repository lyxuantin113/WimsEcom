package com.wims.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@Builder
public class PageResponse<T> {
    private int currentPage; // Trang hiện tại (VD: 1)
    private int totalPages;  // Tổng số trang (VD: 10)
    private int pageSize;    // Số lượng phần tử trong 1 trang (VD: 10)
    private long totalElements; // Tổng số bản ghi trong DB (VD: 100)

    @Builder.Default
    private List<T> data = Collections.emptyList(); // Dữ liệu chính (List Product, User...)
}