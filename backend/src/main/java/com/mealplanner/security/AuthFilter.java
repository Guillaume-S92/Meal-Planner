package com.mealplanner.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealplanner.exception.ApiError;
import com.mealplanner.exception.UnauthorizedException;
import com.mealplanner.repository.AppUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthFilter extends OncePerRequestFilter {

    public static final String AUTHENTICATED_USER_ATTRIBUTE = "authenticatedUser";
    private final AuthTokenService tokenService;
    private final AppUserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AuthFilter(AuthTokenService tokenService, AppUserRepository userRepository, ObjectMapper objectMapper) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || !path.startsWith("/api/")
                || "/api/auth/login".equals(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            unauthorized(response, "Authentification requise.");
            return;
        }

        try {
            AuthenticatedUser tokenUser = tokenService.parse(authorization.substring(7));
            userRepository.findByUsernameIgnoreCase(tokenUser.username())
                    .filter(user -> user.isEnabled() && user.getId().equals(tokenUser.id()))
                    .orElseThrow(() -> new UnauthorizedException("Utilisateur non autorise."));
            request.setAttribute(AUTHENTICATED_USER_ATTRIBUTE, tokenUser);
            filterChain.doFilter(request, response);
        } catch (UnauthorizedException exception) {
            unauthorized(response, exception.getMessage());
        }
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), new ApiError(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                message,
                List.of()
        ));
    }
}
