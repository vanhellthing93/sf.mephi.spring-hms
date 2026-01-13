package sf.mephi.booking.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import sf.mephi.common.constants.Role;
import sf.mephi.booking.entity.User;
import sf.mephi.booking.repository.UserRepository;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Initializing test data...");

        if (userRepository.count() > 0) {
            log.info("Data already initialized, skipping...");
            return;
        }

        User testUser = User.builder()
                .username("testuser")
                .password(passwordEncoder.encode("password123"))
                .role(Role.USER)
                .build();
        userRepository.save(testUser);
        log.info("Created test user: testuser (password: password123)");

        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build();
        userRepository.save(admin);
        log.info("Created admin user: admin (password: admin123)");

        log.info("Test data initialization completed!");
    }
}
