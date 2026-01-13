package sf.mephi.booking.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for Booking Service.
 * Provides interactive API documentation with JWT authentication support.
 *
 * Access Swagger UI at: http://localhost:8082/swagger-ui.html
 * Access OpenAPI JSON at: http://localhost:8082/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:booking-service}")
    private String applicationName;

    @Value("${server.port:8082}")
    private String serverPort;

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI bookingServiceOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("API Gateway")
                ))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(
                        new Components()
                                .addSecuritySchemes(SECURITY_SCHEME_NAME, createSecurityScheme())
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("Hotel Booking Service API")
                .description("""
                        Microservice for managing hotel bookings with SAGA pattern implementation.
                        
                        ## Key Features:
                        - **SAGA Pattern**: Distributed transaction management with compensation
                        - **Idempotency**: Request deduplication via requestId
                        - **JWT Security**: Role-based access control (USER/ADMIN)
                        - **Resilience**: Retry, Circuit Breaker, Timeout patterns
                        - **Correlation Tracking**: Request tracing across services
                        
                        ## Authentication:
                        1. Register a new user: `POST /api/v1/auth/register`
                        2. Login to get JWT token: `POST /api/v1/auth/login`
                        3. Click "Authorize" button and enter: `Bearer <your-token>`
                        4. All authenticated endpoints will now work
                        
                        ## Booking Flow:
                        1. Create booking in PENDING status
                        2. Call Hotel Service to confirm availability
                        3. Update status to CONFIRMED (success) or CANCELLED (failure)
                        4. Compensation executed automatically on failure
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("VanHellthing")
                        .email("vanhellthing@mail.ru")
                        .url("https://github.com/vanhellthing93/sf.mephi.spring-hms"))
                .license(new License()
                        .name("Educational Project")
                        .url("https://github.com/vanhellthing93/sf.mephi.spring-hms/blob/main/LICENSE"));
    }

    private SecurityScheme createSecurityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter JWT token obtained from /api/v1/auth/login endpoint");
    }
}
