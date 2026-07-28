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

import com.allan.security.oauth2.CustomOAuth2UserService;
import com.allan.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.allan.security.oauth2.OAuth2AuthenticationSuccessHandler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor  // ✅ inject OAuth2 dependencies via constructor
public class SecurityConfig {

    // ✅ injected — required for oauth2Login block
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2FailureHandler;

    // ─────────────────────────────────────────────
    // SECURITY FILTER CHAIN
    // ─────────────────────────────────────────────

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        http
            // ✅ IF_REQUIRED — allows OAuth2 to maintain session state
            // during the Google redirect flow (state parameter, auth code).
            // JWT-based API endpoints remain effectively stateless because
            // JwtTokenValidator sets the SecurityContext from the token on
            // every request, so the session is never actually used for API auth.
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

            .authorizeHttpRequests(auth -> auth

                // ── Public admin auth ────────────────────────────
                // Must be before /api/admin/** ROLE_ADMIN rule —
                // first match wins
                .requestMatchers(
                        "/api/admin/create",
                        "/api/admin/login",
                        "/api/admin/verify/**"
                ).permitAll()

                // ── Public OAuth2 endpoints ──────────────────────
                // Must be before .authenticated() catch-all rules
                .requestMatchers(
                        "/api/auth/google",
                        "/login/oauth2/code/google",
                        "/oauth2/**",
                        "/auth/**"
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

                // ── Public home/category endpoints ───────────────
                .requestMatchers(
                        "/home",
                        "/home/**",
                        "/api/home/**",
                        "/home/categories",   // ✅ added leading slash
                        "/home/flash-sales"
                ).permitAll()

                // ── Public product/review endpoints ──────────────
                .requestMatchers(
                        "/api/products/*/reviews",
                        "/api/products/**"
                ).permitAll()

                // ── Admin only ───────────────────────────────────
                // Covers /api/admin/home-category/** automatically —
                // no separate rule needed
                .requestMatchers("/api/admin/**")
                        .hasAuthority("ROLE_ADMIN")

                // ── Admin home-category (non /api prefix) ────────
                // ✅ fixed: was missing from original, added here
                .requestMatchers(
                        "/admin/home-category",
                        "/admin/home-category/**"
                ).hasAuthority("ROLE_ADMIN")

                // ── Seller only ──────────────────────────────────
                .requestMatchers("/api/seller/**")
                        .hasAuthority("ROLE_SELLER")

                // ── Authenticated OAuth2 management ─────────────
                .requestMatchers(
                        "/api/auth/google/complete-profile",
                        "/api/auth/google/profile-status",
                        "/api/auth/google/auth-methods",
                        "/api/auth/google/link",
                        "/api/auth/google/unlink"
                ).authenticated()

                // ── Authenticated customer endpoints ─────────────
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

            // ✅ JWT filter — validates Bearer tokens on every request
            .addFilterBefore(new JwtTokenValidator(),
                    BasicAuthenticationFilter.class)

            // ✅ oauth2Login is a TOP-LEVEL http method — NOT inside
            // authorizeHttpRequests. This was the primary structural error.
            .oauth2Login(oauth2 -> oauth2
                    .authorizationEndpoint(endpoint -> endpoint
                            .baseUri("/api/auth/google")
                    )
                    .redirectionEndpoint(endpoint -> endpoint
                            .baseUri("/login/oauth2/code/*")
                    )
                    .userInfoEndpoint(endpoint -> endpoint
                            .userService(customOAuth2UserService)
                    )
                    .successHandler(oAuth2SuccessHandler)
                    .failureHandler(oAuth2FailureHandler)
            )

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
            public CorsConfiguration getCorsConfiguration(
                    HttpServletRequest request) {
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