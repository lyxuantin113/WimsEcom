package com.wims.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Nếu field nào null thì bỏ qua, không trả về
public class ApiResponse<T> {

    @Builder.Default
    private int code = 1000; // Mặc định 1000 là Thành công (Quy ước riêng của dự án)

    private String message;  // Thông báo lỗi hoặc thành công

    private T result;        // Dữ liệu trả về (Product, User, List...)
}