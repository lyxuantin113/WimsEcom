package com.wims.backend.service.based;

import com.wims.backend.dto.request.LoginRequest;
import com.wims.backend.dto.response.LoginResponse;
import com.wims.backend.entity.Role;
import com.wims.backend.entity.User;
import com.wims.backend.exception.AppException;
import com.wims.backend.repository.UserRepository;
import com.wims.backend.security.JwtTokenProvider;
import com.wims.backend.service.infrastructure.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setName("USER");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encoded_password");
        user.setRoles(Set.of(role));
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest("testuser", "password");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded_password")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken("testuser")).thenReturn("access_token");
        when(jwtTokenProvider.generateRefreshToken("testuser")).thenReturn("refresh_token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access_token", response.token());
        assertEquals("refresh_token", response.refreshToken());
        assertEquals("testuser", response.username());
        verify(redisService).save(eq("refresh_token:testuser"), eq("refresh_token"), anyLong(), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void login_UserNotFound() {
        LoginRequest request = new LoginRequest("unknown", "password");
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> authService.login(request));
    }

    @Test
    void refreshToken_Success() {
        String refreshToken = "valid_refresh_token";
        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(refreshToken)).thenReturn("testuser");
        when(redisService.get("refresh_token:testuser")).thenReturn(refreshToken);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken("testuser")).thenReturn("new_access_token");

        LoginResponse response = authService.refreshToken(refreshToken);

        assertNotNull(response);
        assertEquals("new_access_token", response.token());
        assertEquals(refreshToken, response.refreshToken());
    }

    @Test
    void refreshToken_InvalidToken() {
        String refreshToken = "invalid_token";
        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(false);

        assertThrows(AppException.class, () -> authService.refreshToken(refreshToken));
    }

    @Test
    void logout_Success() {
        String accessToken = "valid_access_token";
        java.util.Date expiry = new java.util.Date(System.currentTimeMillis() + 10000);

        when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(accessToken)).thenReturn("testuser");
        when(jwtTokenProvider.getExpirationFromToken(accessToken)).thenReturn(expiry);

        authService.logout(accessToken);

        verify(redisService).delete("refresh_token:testuser");
        verify(redisService).save(startsWith("blacklist_token:"), eq("true"), anyLong(), eq(TimeUnit.MILLISECONDS));
    }
}
