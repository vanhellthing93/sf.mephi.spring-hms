package sf.mephi.common.security;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConstantsTest {
    @Test
    void constants_ShouldHaveCorrectValues() {
        assertEquals("your-very-secure-secret-key-at-least-256-bits-long-for-hs256-algorithm", SecurityConstants.JWT_SECRET);
        assertEquals(70, SecurityConstants.JWT_SECRET.length());
        assertEquals(3600000L, SecurityConstants.JWT_EXPIRATION_MS);
        assertEquals("Authorization", SecurityConstants.JWT_HEADER);
        assertEquals("Bearer ", SecurityConstants.JWT_PREFIX);

        assertEquals("ROLE_USER", SecurityConstants.ROLE_USER);
        assertEquals("ROLE_ADMIN", SecurityConstants.ROLE_ADMIN);
    }

    @Test
    void utilityClass_ShouldBeUninstantiable() {
        assertThrows(UnsupportedOperationException.class,
                () -> {
                    try {
                        var constructor = SecurityConstants.class.getDeclaredConstructor();
                        constructor.setAccessible(true);
                        constructor.newInstance();
                    } catch (InvocationTargetException e) {
                        throw (UnsupportedOperationException) e.getCause();
                    }
                });
    }
}
