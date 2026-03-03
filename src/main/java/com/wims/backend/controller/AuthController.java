package com.wims.backend.controller;

import com.wims.backend.dto.ApiResponse;
import com.wims.backend.dto.request.LoginRequest;
import com.wims.backend.dto.request.RegisterRequest;
import com.wims.backend.dto.response.LoginResponse;
import com.wims.backend.dto.response.RegisterResponse;
import com.wims.backend.dto.response.TokenResponse;
import com.wims.backend.service.based.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpiration;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        TokenResponse tokenResponse = authService.login(request);
        setRefreshTokenCookie(response, tokenResponse.refreshToken());

        LoginResponse loginResponse = LoginResponse.builder()
                .token(tokenResponse.accessToken())
                .username(tokenResponse.username())
                .role(tokenResponse.role())
                .build();

        return ApiResponse.success(loginResponse).build();
    }

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request)).build();
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        TokenResponse tokenResponse = authService.refreshToken(refreshToken);
        setRefreshTokenCookie(response, tokenResponse.refreshToken());

        LoginResponse loginResponse = LoginResponse.builder()
                .token(tokenResponse.accessToken())
                .username(tokenResponse.username())
                .role(tokenResponse.role())
                .build();

        return ApiResponse.success(loginResponse).build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader("Authorization") String token,
            HttpServletResponse response) {
        if (token != null && token.startsWith("Bearer ")) {
            authService.logout(token.substring(7));
        }
        clearRefreshTokenCookie(response);
        return ApiResponse.<Void>success(null).message("Logout thành công").build();
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Đổi thành true nếu chạy HTTPS
        cookie.setPath("/");
        cookie.setMaxAge((int) (refreshTokenExpiration / 1000));
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}