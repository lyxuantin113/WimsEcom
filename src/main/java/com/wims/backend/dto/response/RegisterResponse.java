package com.wims.backend.dto.response;

import lombok.Builder;

@Builder
public record RegisterResponse(
        String username,
        String email,
        String fullname) {
}
