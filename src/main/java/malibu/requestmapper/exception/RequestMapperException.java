package malibu.requestmapper.exception;

public class RequestMapperException extends RuntimeException {

    public RequestMapperException() {
        super();
    }

    public RequestMapperException(String msg) {
        super(msg);
    }

    public RequestMapperException(Throwable cause) {
        super(cause);
    }

    public RequestMapperException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
