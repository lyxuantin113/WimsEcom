package com.wims.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.wims.backend.service.infrastructure.RedisService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final RedisService redisService;

    private static final String BLACKLIST_PREFIX = "blacklist_token:";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. Lấy JWT từ request (Header: Authorization)
            String jwt = getJwtFromRequest(request);

            // 2. Validate Token & Check Blacklist
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {

                // CHECK BLACKLIST
                if (redisService.hasKey(BLACKLIST_PREFIX + jwt)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // 3. Lấy username từ chuỗi token
                String username = jwtTokenProvider.getUsernameFromToken(jwt);

                // 4. Load thông tin User từ Database lên (để lấy Role)
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                // 5. Nếu người dùng hợp lệ, set thông tin cho Security Context
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                // lƯU Audit & Log. Ví dụ: User Admin đã đăng nhập từ IP 192.168.1.1
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // CHỐT: Đã xác thực thành công, lưu vào Context để Spring biết người này là ai
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            System.err.println("Could not set user authentication in security context: " + ex.getMessage());
        }

        // 6. Cho phép request đi tiếp vào Controller (hoặc Filter tiếp theo)
        filterChain.doFilter(request, response);
    }

    // Hàm phụ trợ để lấy token từ header "Authorization: Bearer <token>"
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}