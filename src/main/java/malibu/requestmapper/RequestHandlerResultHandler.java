package malibu.requestmapper;

/**
 * @param <O> - outputContext type
 */
public interface RequestHandlerResultHandler<O> {

    boolean isSupports(RequestHandlerResult handlerResult);

    /**
     * handler가 null을 return 했을때의 처리도 해야한다.
     * @param handlerResult
     * @return
     */
    O handleResult(RequestHandlerResult handlerResult);
}
