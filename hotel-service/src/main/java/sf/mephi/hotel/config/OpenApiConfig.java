package sf.mephi.hotel.config;

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
 * OpenAPI/Swagger configuration for Hotel Service.
 *
 * Access Swagger UI at: http://localhost:8081/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8081}")
    private String serverPort;

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI hotelServiceOpenAPI() {
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
                .title("Hotel Management Service API")
                .description("""
                        Microservice for managing hotels and rooms with load balancing algorithm.
                        
                        ## Key Features:
                        - **CRUD Operations**: Hotels and Rooms management
                        - **Smart Allocation**: Rooms sorted by timesBooked (least used first)
                        - **Statistics**: Room booking analytics
                        - **Optimistic Locking**: Concurrent booking handling via @Version
                        - **Availability Management**: Confirm/Release room slots
                        
                        ## Room Recommendation Algorithm:
                        Rooms are recommended based on:
                        1. Available = true
                        2. Sort by timesBooked ASC (least booked first)
                        3. Sort by id ASC (tie-breaker)
                        
                        This ensures even distribution and prevents room "starvation".
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("VanHellthing")
                        .email("vanhellthing@mail.ru")
                        .url("https://github.com/vanhellthing93/sf.mephi.spring-hms"))
                .license(new License()
                        .name("Educational Project"));
    }

    private SecurityScheme createSecurityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT token from authentication service");
    }
}
