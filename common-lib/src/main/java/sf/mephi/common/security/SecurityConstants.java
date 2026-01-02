package sf.mephi.common.security;

import sf.mephi.common.constants.ApiConstants;
import sf.mephi.common.constants.Role;

public final class SecurityConstants {
    public static final String JWT_SECRET = "your-very-secure-secret-key-at-least-256-bits-long-for-hs256-algorithm";
    public static final long JWT_EXPIRATION_MS = 3600000; // 1 час
    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_PREFIX = "Bearer ";

    // Используем enum Role
    public static final String ROLE_USER = Role.USER.getAuthority();
    public static final String ROLE_ADMIN = Role.ADMIN.getAuthority();

    // Публичные endpoints (без аутентификации) - обновлены для /api/v1
    public static final String[] PUBLIC_ENDPOINTS = {
            ApiConstants.USER_REGISTER_FULL_PATH,
            ApiConstants.USER_AUTH_FULL_PATH,
            ApiConstants.HOTELS_FULL_PATH,
            ApiConstants.HOTELS_FULL_PATH + "/**",
            ApiConstants.ROOMS_FULL_PATH,
            ApiConstants.ROOMS_FULL_PATH + "/**",
            "/actuator/health",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    private SecurityConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}
