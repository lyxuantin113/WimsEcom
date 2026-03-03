package com.wims.backend.dto.response;

import lombok.Builder;

@Builder
public record LoginResponse(
                String token,
                String username,
                String role) {
}
