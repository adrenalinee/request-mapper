package malibu.requestmapper.exception;

import malibu.requestmapper.HandlerMethod;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 1. RequestHandlerInterceptor.preHandle(),
 * 2. RequestHandlerInterceptor.postHandle(),
 *
 * 3. handler method invoke
 *
 * 위 3곳에서 에러가 발생했음을 알린다.
 *
 */
@Accessors(chain = true)
public class RequestExecutionException extends RequestMapperException {

    @Setter
    @Getter
    private HandlerMethod handlerMethod;

//    /**
//     * 발생한 에러
//     */
//    @Setter
//    @Getter
//    private Throwable risedThrowable;

    public RequestExecutionException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
