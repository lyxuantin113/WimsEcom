package com.wims.backend.controller;

import com.wims.backend.dto.ApiResponse;
import com.wims.backend.dto.request.LoginRequest;
import com.wims.backend.dto.request.RegisterRequest;
import com.wims.backend.dto.response.LoginResponse;
import com.wims.backend.dto.response.RegisterResponse;
import com.wims.backend.service.based.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request)).build();
    }

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request)).build();
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@RequestParam("refreshToken") String refreshToken) {
        return ApiResponse.success(authService.refreshToken(refreshToken)).build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            authService.logout(token.substring(7));
        }
        return ApiResponse.<Void>success(null).message("Logout thành công").build();
    }
}