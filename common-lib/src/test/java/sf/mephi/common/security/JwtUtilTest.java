package sf.mephi.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Test
    void generateToken_ShouldIncludeUsernameAndRoles() {
        List<String> roles = List.of("USER", "ADMIN");

        String token = jwtUtil.generateToken("testuser", roles);

        assertNotNull(token);
        assertTrue(jwtUtil.isTokenValid(token, "testuser"));
        assertEquals("testuser", jwtUtil.extractUsername(token));
        assertEquals(2, jwtUtil.extractRoles(token).size());
        assertTrue(jwtUtil.extractRoles(token).contains("USER"));
    }
}
