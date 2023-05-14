package malibu.requestmapper.exception;

/**
 * handler 가 응답한 객체의 ResultHandler 를 찾을 수 없을때 throw 됨.
 */
public class ResultRequestNotFoundException extends RequestMapperException {

    public ResultRequestNotFoundException() {
        super();
    }

    public ResultRequestNotFoundException(String msg) {
        super(msg);
    }

    public ResultRequestNotFoundException(Throwable cause) {
        super(cause);
    }

    public ResultRequestNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
