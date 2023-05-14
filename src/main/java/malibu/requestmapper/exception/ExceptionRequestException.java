package malibu.requestmapper.exception;

import malibu.requestmapper.HandlerMethod;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * exeption을 처리하는 exception handler method가 에러를 발생시켰음을 알림.
 */
@Accessors(chain = true)
public class ExceptionRequestException extends RequestMapperException {

    @Setter
    @Getter
    private HandlerMethod handlerMethod;

    /**
     * 발생한 에러
     */
    @Setter
    @Getter
    private Throwable risedThrowable;

    @Setter
    @Getter
    private Throwable handlerThrowable;

    public ExceptionRequestException() {
        super();
    }

    public ExceptionRequestException(String msg) {
        super(msg);
    }

    public ExceptionRequestException(Throwable cause) {
        super(cause);
    }

    public ExceptionRequestException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
