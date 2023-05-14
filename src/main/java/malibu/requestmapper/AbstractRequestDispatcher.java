package malibu.requestmapper;

import malibu.requestmapper.exception.AlreayBootstrapedException;
import malibu.requestmapper.exception.BootstrapFailException;
import malibu.requestmapper.exception.RequestMapperConfigurerNotFoundException;
import malibu.requestmapper.util.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 *
 * @param <I> - inputContext type
 * @param <O> - outputContext type
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractRequestDispatcher<I, O> {

//    @NonNull
//    protected final Class<I> inputClass;
//
//    @NonNull
//    protected final Class<O> outputClass;

    /**
     * bootstrap() 호출되었는지확인.
     * 현재 bootstrap 은 한번만 가능
     */
    @Getter
    private boolean bootstrapped;

    /**
     *
     */
    protected Collection<Object> handlerObjects = new ArrayList<>();

    /**
     *
     */
    protected final RequestHandlerMappings<I> handlerMappings = new RequestHandlerMappings<>(handlerObjects);

    /**
     *
     */
    protected final RequestHandlerMethodInvoker<I> handlerMethodInvoker = new RequestHandlerMethodInvoker<>(handlerObjects);

    /**
     *
     */
    protected final RequestHandlerResultHandlers<O> handlerResultHandlers = new RequestHandlerResultHandlers<>();

    /**
     *
     */
    protected final InterceptorRegistry<I, O> interceptorRegistry = new InterceptorRegistry<>();

    /**
     *
     */
    protected final List<RequestMapperConfigurer<I, O>> mapperConfigurers = Lists.newArrayList();

    /**
     * handler mapper 초기화
     * 모든 확장 포인트를 등록하고 마지막에 호출해야 함.
     */
    public void bootstrap() {
        if (log.isTraceEnabled()) {
            log.trace("start");
        }

        if (bootstrapped) {
            throw new AlreayBootstrapedException();
        }

        if (mapperConfigurers.isEmpty()) {
            throw new RequestMapperConfigurerNotFoundException();
//            mapperConfigurers.add(new ByPassHandlerMapperConfiguration<>(inputClass, outputClass));
        }

        mapperConfigurers.forEach(configurer -> {
            tryAction(() -> configurer.configreInterceptors(interceptorRegistry));
            tryAction(() -> configurer.configureInterceptors(interceptorRegistry));
            tryAction(() -> configurer.configureTypeConverters(TypeConverterRegistry.getInstance()));
            tryAction(() -> configurer.configureHandlerMappings(handlerMappings));
            tryAction(() -> configurer.configureHandlerResultHandlers(handlerResultHandlers));
            tryAction(() -> configurer.configureHandlerMethodInvoker(handlerMethodInvoker));
        });

        handlerMappings.detectRequestHandlerMethods();
        handlerMethodInvoker.detectExceptionHandlerMethods();

        bootstrapped = true;
    }

    protected RequestHandlerExecutionChain<I, O> createHandlerExecutionChain(HandlerMethod handlerMethod) {
        final RequestHandlerExecutionChain<I, O> executionChain = new RequestHandlerExecutionChain<>(handlerMethod);
        interceptorRegistry.getInterceptors()
                           //TODO handler 별로 실행될지 말지 filtering 되어야함. 지금은 등록된 interceptor가 무조건 모두 호출됨
                           .forEach(interceptor -> executionChain.addInterceptor(interceptor));

        return executionChain;
    }

    protected void tryAction(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            throw new BootstrapFailException("bootstrapping 작업에 실패하였습니다.", t);
        }
    }

    protected <T> void tryAction(Consumer<T> consumer, T type) {
        try {
            consumer.accept(type);
        } catch (Throwable t) {
            throw new BootstrapFailException("bootstrapping 작업에 실패하였습니다.", t);
        }
    }

    /**
     *
     * @param mapperConfigurer
     * @return
     */
    public AbstractRequestDispatcher<I, O> addRequestMapperConfigurer(RequestMapperConfigurer<I, O> mapperConfigurer) {
        if (mapperConfigurers.contains(mapperConfigurer)) {
            throw new RuntimeException("이미 등록된 mapperConfigurer 입니다. mapperConfigurer: " + mapperConfigurer);
        }

        mapperConfigurers.add(mapperConfigurer);
        return this;
    }

    /**
     *
     * @param exceptionHandlerMappingsConsumer
     * @return
     */
    public AbstractRequestDispatcher<I, O> configureExceptionHandlerMappings(Consumer<ExceptionHandlerMappings> exceptionHandlerMappingsConsumer) {
        tryAction(exceptionHandlerMappingsConsumer, handlerMethodInvoker.getExceptionHandlerMappings());
        return this;
    }

    /**
     * request, exception들을 handling 할 method를 포함하고 있는 class의 instance를 등록한다.
     *
     * @param handlerObject
     * @return
     */
    public AbstractRequestDispatcher<I, O> registerHandlerObject(Object handlerObject) {
        handlerObjects.add(handlerObject);
        return this;
    }

    public AbstractRequestDispatcher<I, O> registerInterceptor(RequestHandlerInterceptor<I, O> interceptor) {
        interceptorRegistry.addInterceptor(interceptor);
        return this;
    }

    public AbstractRequestDispatcher<I, O> registerHandlerResultHandler(RequestHandlerResultHandler<O> handlerResultHandler) {
        handlerResultHandlers.addHandlerResultHandler(handlerResultHandler);
        return this;
    }

    public AbstractRequestDispatcher<I, O> registerMethodArgumentResolver(RequestMethodArgumentResolver<I> argumentResolvers) {
        handlerMethodInvoker.addMethodArgumentResolver(argumentResolvers);
        return this;
    }

    public AbstractRequestDispatcher<I, O> registerMappingConditionCreator(RequestMappingConditionCreator<I> mappingConditionCreator) {
        handlerMappings.addMappingConditionCreator(mappingConditionCreator);
        return this;
    }

    public <T> AbstractRequestDispatcher<I, O> registerTypeConverter(Class<T> requiredType, TypeConverter<T> typeConverter) {
        TypeConverterRegistry.getInstance()
                             .addCustomConverter(requiredType, typeConverter);
        return this;
    }
}
