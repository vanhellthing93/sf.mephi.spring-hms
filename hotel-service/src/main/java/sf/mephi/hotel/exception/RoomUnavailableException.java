package sf.mephi.hotel.exception;

import sf.mephi.common.exception.BaseException;

public class RoomUnavailableException extends BaseException {
    public RoomUnavailableException(String message) {
        super(message, 409); // Conflict
    }
}
