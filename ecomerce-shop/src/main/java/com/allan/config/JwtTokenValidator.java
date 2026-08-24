package com.allan.config;

import java.io.IOException;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Populates SecurityContext from a Bearer JWT if one is present and
 * valid. Runs on every request (registered before BasicAuthenticationFilter
 * in SecurityConfig), so it must never hard-fail a request itself —
 * whether a request is actually allowed to proceed is
 * authorizeHttpRequests' job, not this filter's. This filter's only
 * job is: "if there's a valid token, say who the caller is."
 *
 * On a missing, malformed, invalid, or expired token, this filter
 * simply leaves SecurityContext unauthenticated and continues the
 * chain. That correctly lets permitAll() routes proceed anonymously,
 * and correctly lets authenticated()-guarded routes get rejected by
 * Spring Security's own entry point (see SecurityConfig's
 * defaultAuthenticationEntryPointFor) rather than by this filter
 * writing a response directly.
 */
@Slf4j
public class JwtTokenValidator extends OncePerRequestFilter {

    private final SecretKey key = Keys.hmacShaKeyFor(JWT_CONSTANT.SECRET_KEY.getBytes());

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String jwt = request.getHeader("Authorization");

        if (jwt != null && jwt.startsWith("Bearer ") && jwt.length() > 7) {
            jwt = jwt.substring(7);

            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(jwt)
                        .getBody();
                String email = String.valueOf(claims.get("email"));
                String authorities = String.valueOf(claims.get("authorities"));

                List<GrantedAuthority> auths = AuthorityUtils
                        .commaSeparatedStringToAuthorityList(authorities);

                Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, auths);

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                // Invalid or expired token — do NOT write to the response
                // and do NOT set an Authentication. Just proceed
                // unauthenticated. authorizeHttpRequests decides what
                // happens next: permitAll() routes work fine as
                // anonymous requests; authenticated()-guarded routes
                // get correctly rejected by Spring Security's own
                // entry point further down the chain.
                //
                // Deliberately not logging the token or claims here —
                // only that validation failed and why, at debug level
                // (expired/malformed tokens are routine, not incidents).
                log.debug("JWT validation failed, proceeding unauthenticated: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

}