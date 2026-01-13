package sf.mephi.hotel.exception;

import org.junit.jupiter.api.Test;
import sf.mephi.common.exception.BaseException;

import static org.junit.jupiter.api.Assertions.*;

class RoomUnavailableExceptionTest {

    @Test
    void constructor_ShouldSetMessageAndStatus409() {
        String message = "Room is not available";

        RoomUnavailableException exception = new RoomUnavailableException(message);

        assertEquals(message, exception.getMessage());
        assertEquals(409, ((BaseException) exception).getStatusCode());
    }

    @Test
    void exception_ShouldBeInstanceOfBaseException() {
        RoomUnavailableException exception = new RoomUnavailableException("Test");

        assertInstanceOf(BaseException.class, exception);
    }
}
