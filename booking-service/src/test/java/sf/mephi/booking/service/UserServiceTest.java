package sf.mephi.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import sf.mephi.common.constants.Role;
import sf.mephi.common.exception.NotFoundException;
import sf.mephi.common.exception.ValidationException;
import sf.mephi.common.security.JwtUtil;
import sf.mephi.booking.dto.request.AuthRequest;
import sf.mephi.booking.dto.request.RegisterRequest;
import sf.mephi.booking.dto.response.AuthResponse;
import sf.mephi.booking.entity.User;
import sf.mephi.booking.mapper.UserMapper;
import sf.mephi.booking.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;
    private AuthRequest authRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        authRequest = AuthRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        user = User.builder()
                .id(1L)
                .username("testuser")
                .password("$2a$10$encodedPassword")
                .role(Role.USER)
                .build();
    }

    @Test
    void register_ShouldCreateUserAndReturnToken() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userMapper.toEntity(registerRequest)).thenReturn(user);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateToken(anyString(), anyList())).thenReturn("jwt-token-123");

        AuthResponse response = userService.register(registerRequest);

        assertNotNull(response);
        assertEquals("jwt-token-123", response.getToken());
        assertEquals("testuser", response.getUsername());
        assertEquals("USER", response.getRole());

        verify(userRepository).existsByUsername("testuser");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken(eq("testuser"), anyList());
    }

    @Test
    void register_ShouldThrowException_WhenUsernameExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(ValidationException.class, () -> userService.register(registerRequest));

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any());
    }

    @Test
    void authenticate_ShouldReturnToken_WhenCredentialsValid() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", user.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyList())).thenReturn("jwt-token-456");

        AuthResponse response = userService.authenticate(authRequest);

        assertNotNull(response);
        assertEquals("jwt-token-456", response.getToken());
        assertEquals("testuser", response.getUsername());
        assertEquals("USER", response.getRole());

        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", user.getPassword());
    }

    @Test
    void authenticate_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> userService.authenticate(authRequest));

        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void authenticate_ShouldThrowException_WhenPasswordInvalid() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", user.getPassword())).thenReturn(false);

        assertThrows(ValidationException.class, () -> userService.authenticate(authRequest));

        verify(passwordEncoder).matches("password123", user.getPassword());
        verify(jwtUtil, never()).generateToken(anyString(), anyList());
    }

    @Test
    void getUserByUsername_ShouldReturnUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        User result = userService.getUserByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void getUserByUsername_ShouldThrowException_WhenNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserByUsername("unknown"));

        verify(userRepository).findByUsername("unknown");
    }
}
