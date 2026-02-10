package com.wims.backend.security;

import com.wims.backend.entity.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.wims.backend.entity.User; // Import Entity của con

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @param user Getter để lấy Entity ra dùng 🌟 Chìa khóa ở đây: Nó chứa Entity User của con
 */
public record CustomUserDetails(User user) implements UserDetails {

    // 👇 Map các hàm của UserDetails vào field của Entity
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return user.getRoles().stream().map(role -> {
            String roleName = role.getName();

            // Kiểm tra xem trong DB đã có chữ ROLE_ chưa để tránh cộng dồn
            if (!roleName.startsWith("ROLE_")) {
                roleName = "ROLE_" + roleName;
            }

            return new SimpleGrantedAuthority(roleName);
        }).collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}