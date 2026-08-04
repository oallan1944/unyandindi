package com.allan.config;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2FailureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        http
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                        "/api/admin/create",
                        "/api/admin/login",
                        "/api/admin/verify/**"
                ).permitAll()

                .requestMatchers(
                        "/api/auth/google",
                        "/login/oauth2/code/google",
                        "/oauth2/**",
                        "/auth/**"
                ).permitAll()

                .requestMatchers(
                        "/api/sellers/login",
                        "/api/sellers/verify/**",
                        "/api/sellers"
                ).permitAll()

                .requestMatchers(
                        "/api/users/login",
                        "/api/users/signup"
                ).permitAll()

                .requestMatchers(
                        "/home",
                        "/home/**",
                        "/api/home/**",
                        "/home/flash-sales"
                ).permitAll()

                .requestMatchers(
                        "/api/products/*/reviews",
                        "/api/products/**"
                ).permitAll()

                .requestMatchers("/api/admin/**")
                        .hasAuthority("ROLE_ADMIN")

                .requestMatchers(
                        "/admin/home-category",
                        "/admin/home-category/**",
                        "/home/categories"
                ).hasAuthority("ROLE_ADMIN")

                .requestMatchers(
                        "/admin/deals",
                        "/admin/deals/**"
                ).hasAuthority("ROLE_ADMIN")

                .requestMatchers("/api/seller/**")
                        .hasAuthority("ROLE_SELLER")

                .requestMatchers(
                        "/api/auth/google/complete-profile",
                        "/api/auth/google/profile-status",
                        "/api/auth/google/auth-methods",
                        "/api/auth/google/link",
                        "/api/auth/google/unlink"
                ).authenticated()

                .requestMatchers(
                        "/api/orders/**",
                        "/api/cart/**",
                        "/api/reviews/**",
                        "/api/wishlist/**",
                        "/api/address/**",
                        "/api/payments/**"
                ).authenticated()

                .anyRequest().permitAll()
            )

            .addFilterBefore(new JwtTokenValidator(),
                    BasicAuthenticationFilter.class)

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

            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .exceptionHandling(exceptions -> exceptions
                    .defaultAuthenticationEntryPointFor(
                            (request, response, authException) ->
                                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"),
                            request -> true
                    )
            );

        return http.build();
    }

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