package com.wims.backend.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            // Lấy thông tin xác thực từ SecurityContext (Nơi Filter vừa nạp vào)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Nếu chưa đăng nhập hoặc không xác định -> Trả về rỗng (hoặc mặc định SYSTEM)
            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
                return Optional.empty();
            }

            // Trả về username của người đang đăng nhập
            return Optional.of(authentication.getName());
        };
    }
}