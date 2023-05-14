package malibu.requestmapper.exception;

/**
 * inputContext 를 처리할 handler 를 못찾았을 경우 throw 됨.
 */
public class MatchedRequestNotFoundException extends RequestMapperException {

    public MatchedRequestNotFoundException(Object inputContext) {
        super("matched request handler not found. inputContext: " + inputContext);
    }
}
