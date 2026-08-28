package com.ragapp.auth;

import java.io.IOException;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Authenticates requests from the {@code Authorization: Bearer <jwt>} header.
 *
 * <p>The token is trusted purely on its signature and expiry, so both registered
 * organization users and anonymous individual sessions authenticate the same way.
 * The resulting {@link RagUserPrincipal} carries the {@code scopeKey} that
 * downstream controllers use to isolate documents and queries.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        try {
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                Claims claims = jwtService.parse(jwt); // verifies signature + expiry

                String username = claims.getSubject();
                String scopeKey = asString(claims.get("scopeKey"));
                String role = asString(claims.get("role"));
                String displayName = asString(claims.get("name"));

                if (scopeKey != null && role != null) {
                    RagUserPrincipal principal = new RagUserPrincipal(username, scopeKey, role, displayName);
                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(principal, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception ignored) {
            // Any malformed, tampered, or expired token — skip authentication.
            // Spring Security rejects the request with 401/403 automatically.
        }

        filterChain.doFilter(request, response);
    }

    private static String asString(Object value) {
        return value == null ? null : value.toString();
    }
}
