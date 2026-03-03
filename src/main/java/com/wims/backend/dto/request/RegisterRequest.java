package com.wims.backend.dto.request;

public record RegisterRequest(
        String username,
        String password,
        String email,
        String fullname) {
}
