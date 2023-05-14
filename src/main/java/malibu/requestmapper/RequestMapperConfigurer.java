package malibu.requestmapper;

/**
 * requestmapper framework 를 사용할때 초기 설정을 여기서 진행.
 *
 * @param <I> - inputContext type
 * @param <O> - outputContext type
 */
public interface RequestMapperConfigurer<I, O> {

    /**
     *
     * @param interceptorRegistry
     * @deprecated - 이름이 오타여서 변경합니다. configureInterceptors() 를 사용하세요.
     */
    @Deprecated
    default void configreInterceptors(InterceptorRegistry<I, O> interceptorRegistry) {}

    default void configureInterceptors(InterceptorRegistry<I, O> interceptorRegistry) {}

    /**
     * 프리미티브 타입이 아닌 커스텀 타입으로 변환 시키려할때custom converter 추가 할 수 있음.
     * @param typeConverterRegistry
     */
    default void configureTypeConverters(TypeConverterRegistry typeConverterRegistry) {}

//    /**
//     * 수동으로 handler mapping 을 추가할 수 있음.
//     * @param handlerMappingRegistry
//     */
//    default void configureHandlerMappings(RequestHandlerMappingRegistry handlerMappingRegistry) {}

    default void configureHandlerMappings(RequestHandlerMappings<I> handlerMappings) {}

    /**
     * requestHandlerMethod 가 처리한 result 객체를 output객체로 변환할 RequestHandlerResultHandler 와 관련된 설정을 한다.
     * @param handlerResultHandlers
     */
    default void configureHandlerResultHandlers(RequestHandlerResultHandlers<O> handlerResultHandlers) {}

    /**
     *
     * @param handlerMethodInvoker
     */
    default void configureHandlerMethodInvoker(RequestHandlerMethodInvoker<I> handlerMethodInvoker) {}

}
