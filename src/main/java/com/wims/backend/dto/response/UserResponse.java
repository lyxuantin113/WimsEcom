package com.wims.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    // Có thể thêm email, fullName nếu muốn
}
