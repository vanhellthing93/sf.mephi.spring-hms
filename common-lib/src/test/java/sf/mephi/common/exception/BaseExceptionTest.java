package sf.mephi.common.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BaseExceptionTest {
    @Test
    void baseException_ShouldSetStatus() {
        class TestEx extends BaseException {
            TestEx(String msg, int code) { super(msg, code); }
        }
        TestEx ex = new TestEx("test", 500);
        assertEquals("test", ex.getMessage());
        assertEquals(500, ((BaseException) ex).getStatusCode());
    }
}
