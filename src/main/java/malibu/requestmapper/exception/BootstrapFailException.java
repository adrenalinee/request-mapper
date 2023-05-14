package malibu.requestmapper.exception;

/**
 * requestmapper 초기화에 실패했을때 throw 됨.
 */
public class BootstrapFailException extends RequestMapperException {

    public BootstrapFailException() {
        super();
    }

    public BootstrapFailException(String msg) {
        super(msg);
    }

    public BootstrapFailException(Throwable cause) {
        super(cause);
    }

    public BootstrapFailException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
