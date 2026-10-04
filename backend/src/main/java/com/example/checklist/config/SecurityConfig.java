package com.example.checklist.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        return http
                // 啟用下面設定的 CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 前後端分離 API 開發階段先關閉 CSRF
                .csrf(csrf -> csrf.disable())

                // 目前先允許所有 API
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").permitAll()
                        .anyRequest().permitAll()
                )

                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // 允許 Angular 開發伺服器：4200 後台、4201 前台 PWA
        config.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "http://localhost:4201"
        ));

        // 允許的 HTTP 方法
        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        // 允許前端送出的 Header
        config.setAllowedHeaders(List.of("*"));

        // 若未來使用 Cookie 或 Authorization，保留 true
        config.setAllowCredentials(true);

        // 預檢結果快取時間，單位為秒
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        // 套用到全部後端路徑
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}