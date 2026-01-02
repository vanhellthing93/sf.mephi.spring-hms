package sf.mephi.common.exception;

import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {
    private final int statusCode;

    protected BaseException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}
