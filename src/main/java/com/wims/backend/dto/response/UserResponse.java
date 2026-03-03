package com.wims.backend.dto.response;

import lombok.Builder;

@Builder
public record UserResponse(
                Long id,
                String username) {
}
