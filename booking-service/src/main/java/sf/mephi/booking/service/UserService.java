package sf.mephi.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sf.mephi.common.constants.ApiConstants;
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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Регистрация нового пользователя
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        // Проверка существования пользователя
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException(
                    String.format(ApiConstants.ERROR_USER_ALREADY_EXISTS, request.getUsername())
            );
        }

        // Создание пользователя
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        User saved = userRepository.save(user);
        log.info("User registered with id: {}", saved.getId());

        // Генерация JWT
        String token = jwtUtil.generateToken(
                saved.getUsername(),
                List.of(saved.getRole().getAuthority())
        );

        return AuthResponse.builder()
                .token(token)
                .username(saved.getUsername())
                .role(saved.getRole().name())
                .build();
    }

    /**
     * Аутентификация пользователя
     */
    @Transactional(readOnly = true)
    public AuthResponse authenticate(AuthRequest request) {
        log.info("Authenticating user: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ValidationException(ApiConstants.ERROR_INVALID_CREDENTIALS));

        // Проверка пароля
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ValidationException(ApiConstants.ERROR_INVALID_CREDENTIALS);
        }

        // Генерация JWT
        String token = jwtUtil.generateToken(
                user.getUsername(),
                List.of(user.getRole().getAuthority())
        );

        log.info("User authenticated: {}", user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    /**
     * Получить пользователя по username
     */
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(
                        String.format(ApiConstants.ERROR_USER_NOT_FOUND, username)
                ));
    }

    /**
     * Получить пользователя по ID
     */
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format(ApiConstants.ERROR_USER_NOT_FOUND, id)
                ));
    }
}
