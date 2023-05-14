package malibu.requestmapper.exception;

public class TypeMissMatchException extends RequestMethodArgResolveFailException {

    public TypeMissMatchException(String message, Throwable cause) {
        super("type converting에 실패하였습니다. " + message, cause);
    }

    public TypeMissMatchException(Throwable cause) {
        super("type converting에 실패하였습니다.", cause);
    }
}
