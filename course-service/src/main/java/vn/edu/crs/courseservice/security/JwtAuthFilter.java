package vn.edu.crs.courseservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secret;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Header JWT phải có dạng:
        // Authorization: Bearer eyJ...
        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            // Bỏ phần "Bearer "
            String token = authHeader.substring(7);

            try {

                // Tạo key từ jwt.secret
                SecretKey key = Keys.hmacShaKeyFor(
                        secret.getBytes(StandardCharsets.UTF_8)
                );

                // Xác thực JWT và lấy payload
                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                // Lấy username từ subject
                String username = claims.getSubject();

                // Lấy role: ADMIN hoặc STUDENT
                String role = claims.get("role", String.class);

                // Chuyển ADMIN -> ROLE_ADMIN
                // STUDENT -> ROLE_STUDENT
                var authToken =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                List.of(
                                        new SimpleGrantedAuthority(
                                                "ROLE_" + role
                                        )
                                )
                        );

                // Đưa thông tin xác thực vào Spring Security
                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authToken);

            } catch (Exception e) {

                // Token sai, hết hạn hoặc chữ ký không hợp lệ
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}