package com.allan.config;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // ─────────────────────────────────────────────
    // SECURITY FILTER CHAIN
    // ─────────────────────────────────────────────

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth

                // ── Public admin auth ────────────────────────────
                .requestMatchers(
                        "/api/admin/create",
                        "/api/admin/login",
                        "/api/admin/verify/**"
                ).permitAll()
                
                // ── Public home/category endpoints ──────────────
                .requestMatchers(
                        "/home/**",
                        "/home/categories",
                        "/api/home/**",
                        "/home/flash-sales"
                ).permitAll()

                // ── Public seller auth ───────────────────────────
                .requestMatchers(
                        "/api/sellers/login",
                        "/api/sellers/verify/**",
                        "/api/sellers"
                ).permitAll()

                // ── Public user auth ─────────────────────────────
                .requestMatchers(
                        "/api/users/login",
                        "/api/users/signup"
                ).permitAll()

                // ── Public product/review endpoints ──────────────
                .requestMatchers(
                        "/api/products/*/reviews",
                        "/api/products/**"
                ).permitAll()

                // ── Admin only ───────────────────────────────────
                .requestMatchers("/api/admin/**")
                        .hasAuthority("ROLE_ADMIN")

                // ── Seller only ──────────────────────────────────
                .requestMatchers("/api/seller/**")
                        .hasAuthority("ROLE_SELLER")

                // ── Authenticated users ──────────────────────────
                .requestMatchers(
                        "/api/orders/**",
                        "/api/cart/**",
                        "/api/reviews/**",
                        "/api/wishlist/**",
                        "/api/address/**",
                        "/api/payments/**"
                ).authenticated()

                // ── Everything else is public ────────────────────
                .anyRequest().permitAll()
            )

            .addFilterBefore(new JwtTokenValidator(), BasicAuthenticationFilter.class)
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }

    // ─────────────────────────────────────────────
    // CORS CONFIGURATION
    // ─────────────────────────────────────────────

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return new CorsConfigurationSource() {
            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                CorsConfiguration cfg = new CorsConfiguration();

                cfg.setAllowedOrigins(List.of(
                        "http://localhost:3000",
                        "http://localhost:4200"
                ));
                cfg.setAllowedMethods(List.of(
                        "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
                ));
                cfg.setAllowedHeaders(Collections.singletonList("*"));
                cfg.setAllowCredentials(true);
                cfg.setExposedHeaders(List.of("Authorization"));
                cfg.setMaxAge(3600L);

                return cfg;
            }
        };
    }
}