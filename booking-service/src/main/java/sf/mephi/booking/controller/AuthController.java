package sf.mephi.booking.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sf.mephi.common.constants.ApiConstants;
import sf.mephi.booking.dto.request.AuthRequest;
import sf.mephi.booking.dto.request.RegisterRequest;
import sf.mephi.booking.dto.response.AuthResponse;
import sf.mephi.booking.service.UserService;

@Slf4j
@RestController
@RequestMapping(ApiConstants.API_V1 + "/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration and authentication")
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(
            summary = "Register new user",
            description = "Creates a new user account and returns JWT token"
    )
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("POST /api/v1/auth/register - username: {}", request.getUsername());
        AuthResponse response = userService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticates user and returns JWT token"
    )
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest request) {

        log.info("POST /api/v1/auth/login - username: {}", request.getUsername());
        AuthResponse response = userService.authenticate(request);
        return ResponseEntity.ok(response);
    }
}
