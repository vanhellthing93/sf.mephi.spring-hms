package sf.mephi.common.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidationExceptionTest {
    @Test
    void constructor_ShouldSetStatus400() {
        ValidationException ex = new ValidationException("Invalid dates");
        assertEquals("Invalid dates", ex.getMessage());
        assertEquals(400, ((BaseException) ex).getStatusCode());
    }
}
