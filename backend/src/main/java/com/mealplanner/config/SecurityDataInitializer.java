package com.mealplanner.config;

import com.mealplanner.domain.AppUser;
import com.mealplanner.repository.AppUserRepository;
import com.mealplanner.security.PasswordHashingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class SecurityDataInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityDataInitializer.class);

    @Bean
    @Transactional
    CommandLineRunner bootstrapAdmin(
            AppUserRepository userRepository,
            PasswordHashingService passwordHashingService,
            @Value("${meal-planner.security.bootstrap-admin.enabled:false}") boolean enabled,
            @Value("${meal-planner.security.bootstrap-admin.username}") String username,
            @Value("${meal-planner.security.bootstrap-admin.password}") String password
    ) {
        return args -> {
            if (!enabled) {
                LOGGER.info("Bootstrap admin desactive.");
                return;
            }
            if (username == null || username.isBlank() || password == null || password.isBlank()) {
                throw new IllegalStateException("Definir MEAL_PLANNER_ADMIN_USERNAME et MEAL_PLANNER_ADMIN_PASSWORD pour creer l'admin.");
            }
            if (userRepository.existsByUsernameIgnoreCase(username)) {
                LOGGER.info("L'utilisateur admin '{}' existe deja, aucune creation.", username);
                return;
            }

            AppUser user = new AppUser();
            user.setUsername(username.trim());
            user.setPasswordHash(passwordHashingService.hash(password));
            user.setRole("ADMIN");
            user.setEnabled(true);
            userRepository.save(user);
            LOGGER.info("Utilisateur admin '{}' cree.", username);
        };
    }
}
