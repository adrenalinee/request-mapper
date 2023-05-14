package malibu.requestmapper;

/**
 * @param <I> - inputContext type
 * @param <O> - outputContext type
 */
public interface RequestHandlerInterceptor<I, O> {

    /**
     *
     * @param inputContext - 필수 전달
     * @param handlerMethod - 필수 전달
     * @return
     */
    default boolean preHandle(I inputContext, HandlerMethod handlerMethod) {
        return true;
    }

    /**
     *
     * @param inputContext - 필수 전달
     * @param handlerMethod - 필수 전달
     * @param handlerReturnValue - null 가능
     */
    default void postHandle(I inputContext, HandlerMethod handlerMethod, Object handlerReturnValue) {

    }

    /**
     *
     * @since 0.1.1
     * @param inputContext - 필수 전달
     * @param handlerMethod - 필수 전달
     * @param outputContext - null 가능
     * @param ex - null 가능
     */
    default void afterCompletion(I inputContext, HandlerMethod handlerMethod, O outputContext, Throwable ex) {

    }
}
