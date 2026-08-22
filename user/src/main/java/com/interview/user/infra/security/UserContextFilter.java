package com.interview.user.infra.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserContextFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            if (SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken auth
                    && auth.isAuthenticated()) {
                Set<String> roles = auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .filter(value -> value.startsWith("ROLE_"))
                        .map(value -> value.substring(5))
                        .collect(Collectors.toUnmodifiableSet());
                UserRequestContext.set(new AuthenticatedUser(
                        auth.getToken().getSubject(),
                        auth.getToken().getClaimAsString("preferred_username"),
                        auth.getToken().getClaimAsString("email"),
                        roles));
            }
            filterChain.doFilter(request, response);
        } finally {
            UserRequestContext.clear();
        }
    }
}
