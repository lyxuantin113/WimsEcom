package com.wims.backend.dto.response;

import lombok.Builder;
import java.util.List;

@Builder
public record PageResponse<T>(
        int currentPage,
        int totalPages,
        int pageSize,
        long totalElements,
        List<T> data) {
}