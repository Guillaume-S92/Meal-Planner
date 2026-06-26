package com.mealplanner.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealplanner.domain.AppUser;
import com.mealplanner.exception.UnauthorizedException;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthTokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthTokenService.class);
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private final ObjectMapper objectMapper;
    private final String jwtSecret;
    private final long validityHours;

    public AuthTokenService(
            ObjectMapper objectMapper,
            @Value("${meal-planner.security.jwt-secret}") String jwtSecret,
            @Value("${meal-planner.security.token-validity-hours}") long validityHours
    ) {
        this.objectMapper = objectMapper;
        this.jwtSecret = jwtSecret;
        this.validityHours = validityHours;
    }

    @PostConstruct
    void warnOnWeakSecret() {
        if (jwtSecret.length() < 32 || jwtSecret.startsWith("dev-")) {
            LOGGER.warn("Le secret JWT est faible. Definir MEAL_PLANNER_JWT_SECRET avant mise en ligne.");
        }
    }

    public String createToken(AppUser user) {
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", user.getUsername());
        payload.put("uid", user.getId());
        payload.put("role", user.getRole());
        payload.put("exp", Instant.now().plus(validityHours, ChronoUnit.HOURS).getEpochSecond());

        String unsignedToken = encodeJson(header) + "." + encodeJson(payload);
        return unsignedToken + "." + sign(unsignedToken);
    }

    public AuthenticatedUser parse(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new UnauthorizedException("Token invalide.");
        }
        String unsignedToken = parts[0] + "." + parts[1];
        if (!sign(unsignedToken).equals(parts[2])) {
            throw new UnauthorizedException("Token invalide.");
        }

        Map<String, Object> payload = decodeJson(parts[1]);
        long expiration = ((Number) payload.get("exp")).longValue();
        if (Instant.now().getEpochSecond() >= expiration) {
            throw new UnauthorizedException("Session expiree.");
        }

        Number userId = (Number) payload.get("uid");
        return new AuthenticatedUser(
                userId.longValue(),
                String.valueOf(payload.get("sub")),
                String.valueOf(payload.get("role"))
        );
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception exception) {
            throw new IllegalStateException("Impossible de creer le token.", exception);
        }
    }

    private Map<String, Object> decodeJson(String value) {
        try {
            byte[] json = DECODER.decode(value);
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception exception) {
            throw new UnauthorizedException("Token invalide.");
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Impossible de signer le token.", exception);
        }
    }
}
