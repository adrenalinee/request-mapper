package malibu.requestmapper.exception;

/**
 *
 */
public class RequestMethodArgResolveFailException extends RequestMapperException {

    public RequestMethodArgResolveFailException(String message, Throwable cause) {
        super("handler method argument resolving 중에 에러가 발생했습니다. " + message, cause);
    }

    public RequestMethodArgResolveFailException(Throwable cause) {
        super("handler method argument resolving 중에 에러가 발생했습니다.", cause);
    }
}
