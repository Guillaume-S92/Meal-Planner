package com.mealplanner.web;

import com.mealplanner.domain.AppUser;
import com.mealplanner.dto.AuthResponse;
import com.mealplanner.dto.CurrentUserResponse;
import com.mealplanner.dto.LoginRequest;
import com.mealplanner.exception.UnauthorizedException;
import com.mealplanner.repository.AppUserRepository;
import com.mealplanner.security.AuthFilter;
import com.mealplanner.security.AuthTokenService;
import com.mealplanner.security.AuthenticatedUser;
import com.mealplanner.security.PasswordHashingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserRepository userRepository;
    private final PasswordHashingService passwordHashingService;
    private final AuthTokenService tokenService;

    public AuthController(
            AppUserRepository userRepository,
            PasswordHashingService passwordHashingService,
            AuthTokenService tokenService
    ) {
        this.userRepository = userRepository;
        this.passwordHashingService = passwordHashingService;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        AppUser user = userRepository.findByUsernameIgnoreCase(request.username())
                .filter(AppUser::isEnabled)
                .orElseThrow(() -> new UnauthorizedException("Identifiants invalides."));

        if (!passwordHashingService.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Identifiants invalides.");
        }

        return new AuthResponse(tokenService.createToken(user), toResponse(user));
    }

    @GetMapping("/me")
    public CurrentUserResponse me(HttpServletRequest request) {
        AuthenticatedUser user = (AuthenticatedUser) request.getAttribute(AuthFilter.AUTHENTICATED_USER_ATTRIBUTE);
        if (user == null) {
            throw new UnauthorizedException("Authentification requise.");
        }
        return new CurrentUserResponse(user.id(), user.username(), user.role());
    }

    private CurrentUserResponse toResponse(AppUser user) {
        return new CurrentUserResponse(user.getId(), user.getUsername(), user.getRole());
    }
}
