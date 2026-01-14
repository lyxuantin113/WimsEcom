package com.wims.backend.utils;

import com.wims.backend.entity.User;
import com.wims.backend.security.CustomUserDetails;
import com.wims.backend.exception.AppException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component // Đánh dấu là Bean để có thể Inject vào Service khác
public class SecurityUtils {

    public User getCurrentUserLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        // Ép kiểu về CustomUserDetails (Record con vừa tạo)
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Trả về Entity User thật
        return userDetails.user();
    }

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;

        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("SCOPE_ADMIN"));
    }
}