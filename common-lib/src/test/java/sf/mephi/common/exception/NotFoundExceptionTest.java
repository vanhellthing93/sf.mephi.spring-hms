package sf.mephi.common.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotFoundExceptionTest {
    @Test
    void constructor_ShouldSetMessageAndStatus() {
        NotFoundException ex = new NotFoundException("Hotel not found");
        assertEquals("Hotel not found", ex.getMessage());
        assertEquals(404, ((BaseException) ex).getStatusCode());
    }
}
