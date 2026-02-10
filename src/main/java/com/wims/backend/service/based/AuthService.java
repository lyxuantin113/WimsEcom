package com.wims.backend.service.based;

import com.wims.backend.dto.request.LoginRequest;
import com.wims.backend.dto.response.LoginResponse;
import com.wims.backend.entity.Role;
import com.wims.backend.entity.User;
import com.wims.backend.exception.AppException;
import com.wims.backend.repository.UserRepository;
import com.wims.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse login(LoginRequest request) {
        // 1. Tìm user trong DB
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(1005, "User không tồn tại"));

        // 2. Kiểm tra mật khẩu (So sánh mật khẩu gửi lên vs mật khẩu mã hóa trong DB)
        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!matches) {
            throw new AppException(986, "Mật khẩu không đúng");
        }

        // 3. Nếu đúng -> Sinh Token trả về
        String token = jwtTokenProvider.generateToken(user.getUsername());

        String roleName = user.getRoles().stream()
                .map(Role::getName)
                .findFirst()
                .orElse("");

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(roleName)
                .build();
    }
}