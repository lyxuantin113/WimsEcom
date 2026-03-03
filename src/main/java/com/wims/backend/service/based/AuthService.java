package com.wims.backend.service.based;

import com.wims.backend.dto.request.LoginRequest;
import com.wims.backend.dto.request.RegisterRequest;
import com.wims.backend.dto.response.LoginResponse;
import com.wims.backend.dto.response.RegisterResponse;
import com.wims.backend.entity.Role;
import com.wims.backend.entity.User;
import com.wims.backend.exception.AppException;
import com.wims.backend.repository.RoleRepository;
import com.wims.backend.repository.UserRepository;
import com.wims.backend.security.JwtTokenProvider;
import com.wims.backend.service.infrastructure.RedisService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String BLACKLIST_PREFIX = "blacklist_token:";

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpiration;

    public LoginResponse login(LoginRequest request) {
        // 1. Tìm user trong DB
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new AppException(1005, "User không tồn tại"));

        // 2. Kiểm tra mật khẩu
        boolean matches = passwordEncoder.matches(request.password(), user.getPassword());
        if (!matches) {
            throw new AppException(986, "Mật khẩu không đúng");
        }

        // 3. Nếu đúng -> Sinh bộ đôi Token
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

        // 4. Lưu Refresh Token vào Redis (7 ngày)
        redisService.save(REFRESH_TOKEN_PREFIX + user.getUsername(), refreshToken, refreshTokenExpiration + 10000,
                java.util.concurrent.TimeUnit.MILLISECONDS);

        String roleName = user.getRoles().stream()
                .map(Role::getName)
                .findFirst()
                .orElse("");

        return LoginResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .role(roleName)
                .build();
    }

    public LoginResponse refreshToken(String refreshToken) {
        // 1. Validate Token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new AppException(1006, "Refresh token không hợp lệ hoặc đã hết hạn");
        }

        // 2. Lấy username từ token
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

        // 3. Kiểm tra trong Redis
        String storedToken = (String) redisService.get(REFRESH_TOKEN_PREFIX + username);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new AppException(1006, "Refresh token không khớp hoặc đã bị thu hồi");
        }

        // 4. Sinh Access Token mới
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(1005, "User không tồn tại"));

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getUsername());

        String roleName = user.getRoles().stream()
                .map(Role::getName)
                .findFirst()
                .orElse("");

        return LoginResponse.builder()
                .token(newAccessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .role(roleName)
                .build();
    }

    public void logout(String accessToken) {
        // 1. Lấy username từ access token (nếu token còn hiệu lực)
        if (jwtTokenProvider.validateToken(accessToken)) {
            String username = jwtTokenProvider.getUsernameFromToken(accessToken);

            // 2. Xóa Refresh Token trong Redis
            redisService.delete(REFRESH_TOKEN_PREFIX + username);

            // 3. Đưa Access Token vào Blacklist (sống cho đến khi hết hạn gốc)
            long expiration = jwtTokenProvider.getExpirationFromToken(accessToken).getTime()
                    - System.currentTimeMillis();
            if (expiration > 0) {
                redisService.save(BLACKLIST_PREFIX + accessToken, "true", expiration,
                        java.util.concurrent.TimeUnit.MILLISECONDS);
            }
        }
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new AppException(9999, "Tên người dùng đã tồn tại, vui lòng chọn tên khác!");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEmail(request.email());
        user.setFullName(request.fullname());

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new AppException(1004, "Role không tồn tại!"));

        user.setRoles(Set.of(userRole));

        userRepository.save(user);

        return RegisterResponse.builder()
                .username(request.username())
                .email(request.email())
                .fullname(request.fullname())
                .build();
    }
}