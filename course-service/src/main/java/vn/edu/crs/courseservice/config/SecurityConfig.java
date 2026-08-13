package vn.edu.crs.courseservice.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import vn.edu.crs.courseservice.security.JwtAuthFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {

        http

                // REST API sử dụng JWT nên tắt CSRF
                .csrf(csrf -> csrf.disable())

                // Không sử dụng session
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // API nội bộ cho registration-service
                        .requestMatchers("/internal/**")
                        .permitAll()

                        // Xem môn học không cần đăng nhập
                        .requestMatchers(
                                HttpMethod.GET,
                                "/courses/**"
                        )
                        .permitAll()

                        // Chỉ ADMIN được thêm môn
                        .requestMatchers(
                                HttpMethod.POST,
                                "/courses/**"
                        )
                        .hasRole("ADMIN")

                        // Chỉ ADMIN được sửa
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/courses/**"
                        )
                        .hasRole("ADMIN")

                        // Chỉ ADMIN được xóa
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/courses/**"
                        )
                        .hasRole("ADMIN")

                        // Endpoint còn lại phải đăng nhập
                        .anyRequest()
                        .authenticated()
                )

                // Cho JwtAuthFilter chạy trước Security filter mặc định
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}