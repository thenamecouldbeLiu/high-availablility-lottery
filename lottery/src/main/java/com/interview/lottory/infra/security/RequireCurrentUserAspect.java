package com.interview.lottory.infra.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
public class RequireCurrentUserAspect {
    private final UserServiceClient userServiceClient;

    public RequireCurrentUserAspect(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @Around("execution(public * com.interview.lottory.controller..*(..)) && " +
            "(@within(com.interview.lottory.infra.security.RequireCurrentUser) || " +
            "@annotation(com.interview.lottory.infra.security.RequireCurrentUser))")
    public Object authorizeAndInitialize(ProceedingJoinPoint joinPoint) throws Throwable {
        JwtAuthenticationToken authentication = requireJwtAuthentication();
        RequireCurrentUser annotation = findAnnotation(joinPoint);
        assertHasAllowedRole(authentication, annotation.roles());

        CurrentUserContext.set(userServiceClient.getOrCreateCurrentUser(
                authentication.getToken().getTokenValue()));
        try {
            return joinPoint.proceed();
        } finally {
            CurrentUserContext.clear();
        }
    }

    private JwtAuthenticationToken requireJwtAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwt && jwt.isAuthenticated()) return jwt;
        throw new AccessDeniedException("Authenticated JWT is required");
    }

    private RequireCurrentUser findAnnotation(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RequireCurrentUser methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(
                method, RequireCurrentUser.class);
        if (methodAnnotation != null) return methodAnnotation;
        return AnnotatedElementUtils.findMergedAnnotation(
                joinPoint.getTarget().getClass(), RequireCurrentUser.class);
    }

    private void assertHasAllowedRole(Authentication authentication, String[] roles) {
        if (roles.length == 0) return;
        boolean allowed = Arrays.stream(roles)
                .map(role -> "ROLE_" + role)
                .anyMatch(role -> authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals(role)));
        if (!allowed) throw new AccessDeniedException("Access is denied");
    }
}
