package sf.mephi.common.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UnauthorizedExceptionTest {
    @Test
    void constructor_ShouldSetStatus401() {
        UnauthorizedException ex = new UnauthorizedException("Unauthorized");
        assertEquals("Unauthorized", ex.getMessage());
        assertEquals(401, ((BaseException) ex).getStatusCode());
    }
}
