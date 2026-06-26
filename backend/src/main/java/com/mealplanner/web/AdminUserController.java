package com.mealplanner.web;

import com.mealplanner.domain.AppUser;
import com.mealplanner.dto.AdminUserCreateRequest;
import com.mealplanner.dto.AdminUserResponse;
import com.mealplanner.dto.AdminUserUpdateRequest;
import com.mealplanner.exception.BusinessRuleException;
import com.mealplanner.exception.ForbiddenException;
import com.mealplanner.exception.ResourceNotFoundException;
import com.mealplanner.exception.UnauthorizedException;
import com.mealplanner.repository.AppUserRepository;
import com.mealplanner.security.AuthFilter;
import com.mealplanner.security.AuthenticatedUser;
import com.mealplanner.security.PasswordHashingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AppUserRepository userRepository;
    private final PasswordHashingService passwordHashingService;

    public AdminUserController(AppUserRepository userRepository, PasswordHashingService passwordHashingService) {
        this.userRepository = userRepository;
        this.passwordHashingService = passwordHashingService;
    }

    @GetMapping
    public List<AdminUserResponse> list(HttpServletRequest request) {
        requireAdmin(request);
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminUserResponse create(@Valid @RequestBody AdminUserCreateRequest request, HttpServletRequest httpRequest) {
        requireAdmin(httpRequest);
        String username = request.username().trim();
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new BusinessRuleException("Cet utilisateur existe deja.");
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(passwordHashingService.hash(request.password()));
        user.setRole(normalizeRole(request.role()));
        user.setEnabled(request.enabled() == null || request.enabled());
        return toResponse(userRepository.save(user));
    }

    @PatchMapping("/{id}")
    public AdminUserResponse update(
            @PathVariable Long id,
            @RequestBody AdminUserUpdateRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedUser currentUser = requireAdmin(httpRequest);
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));

        if (request.enabled() != null) {
            if (currentUser.id().equals(id) && !request.enabled()) {
                throw new BusinessRuleException("Tu ne peux pas desactiver ton propre compte.");
            }
            user.setEnabled(request.enabled());
        }
        if (request.role() != null && !request.role().isBlank()) {
            user.setRole(normalizeRole(request.role()));
        }
        if (request.password() != null && !request.password().isBlank()) {
            if (request.password().length() < 8) {
                throw new BusinessRuleException("Le mot de passe doit contenir au moins 8 caracteres.");
            }
            user.setPasswordHash(passwordHashingService.hash(request.password()));
        }
        return toResponse(userRepository.save(user));
    }

    private AuthenticatedUser requireAdmin(HttpServletRequest request) {
        AuthenticatedUser user = (AuthenticatedUser) request.getAttribute(AuthFilter.AUTHENTICATED_USER_ATTRIBUTE);
        if (user == null) {
            throw new UnauthorizedException("Authentification requise.");
        }
        if (!"ADMIN".equals(user.role())) {
            throw new ForbiddenException("Droits administrateur requis.");
        }
        return user;
    }

    private String normalizeRole(String role) {
        String normalized = role == null || role.isBlank()
                ? "USER"
                : role.trim().toUpperCase(Locale.ROOT);
        if (!"USER".equals(normalized) && !"ADMIN".equals(normalized)) {
            throw new BusinessRuleException("Role invalide.");
        }
        return normalized;
    }

    private AdminUserResponse toResponse(AppUser user) {
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
