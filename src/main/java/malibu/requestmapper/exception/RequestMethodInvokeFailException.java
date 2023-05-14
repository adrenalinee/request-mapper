package malibu.requestmapper.exception;

import malibu.requestmapper.HandlerMethod;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * handler method를 호출하는데 실패했음을 알림.
 */
@Accessors(chain = true)
public class RequestMethodInvokeFailException extends RequestMapperException {

    @Setter
    @Getter
    private HandlerMethod handlerMethod;

    /**
     * 발생한 에러
     */
    @Setter
    @Getter
    private Throwable risedThrowable;

    public RequestMethodInvokeFailException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
