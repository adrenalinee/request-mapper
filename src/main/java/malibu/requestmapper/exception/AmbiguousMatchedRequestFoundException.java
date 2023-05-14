package malibu.requestmapper.exception;

public class AmbiguousMatchedRequestFoundException extends RequestMapperException {

    public AmbiguousMatchedRequestFoundException(Object inputContext) {
        super("ambiguous matched request handler founded. require only one handler. inputContext: " + inputContext);
    }
}
