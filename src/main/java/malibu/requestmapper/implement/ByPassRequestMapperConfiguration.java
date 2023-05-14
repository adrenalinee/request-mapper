package malibu.requestmapper.implement;

import malibu.requestmapper.annotation.RequestMapping;
import lombok.RequiredArgsConstructor;
import malibu.requestmapper.*;

/**
 * 모든 요청을 단일 handler method 가 처리할 수 있게 해준다.
 * handler method 의 argument 로는 inputClass 타입만 가능.
 * handler method 의 return value 로는 outputClass 타입과, void 만 가능.
 *
 * 아무 설정없이 바로 기본적인 동작을 해보려고 할때 사용한다.
 * configuration 이 하나도 등록되지 않으면 이거를 기본값으로 사용한다.
 *
 * @param <I> - inputContext type
 * @param <O> - outputContext type
 */
@RequiredArgsConstructor
public class ByPassRequestMapperConfiguration<I, O> implements RequestMapperConfigurer<I, O> {

    private final Class<I> inputClass;

    private final Class<O> outputClass;

    @Override
    public void configureHandlerResultHandlers(RequestHandlerResultHandlers<O> resultHandlers) {
        resultHandlers.addHandlerResultHandler(new ByPassRequestHandlerResultHandler<>(outputClass));
        resultHandlers.addHandlerResultHandler(new VoidRequestHandlerResultHandler<>(outputClass));
    }

    @Override
    public void configureHandlerMethodInvoker(RequestHandlerMethodInvoker<I> handlerMethodInvoker) {
        handlerMethodInvoker.addMethodArgumentResolver(new ByPassRequestMethodArgumentResolver<>(inputClass));
    }

    @Override
    public void configureHandlerMappings(RequestHandlerMappings<I> handlerMappings) {
        handlerMappings.addMappingConditionCreator(new ByPassMappingConditionCreator<>());
    }
}

/**
 *
 * @param <I> - inputContext type
 */
class ByPassMappingConditionCreator<I> implements RequestMappingConditionCreator<I> {

    @Override
    public boolean isRequire(HandlerMethod handlerMethod) {
        return handlerMethod.getMethod().getAnnotation(RequestMapping.class) != null;
    }

    @Override
    public RequestMappingCondition<I> createCondition(HandlerMethod handlerMethod) {
        return new ByPassMappingCondition<>();
    }
}

/**
 *
 * @param <I> - inputContext type
 */
class ByPassMappingCondition<I> implements RequestMappingCondition<I> {

    @Override
    public boolean isMatchingCondition(I inputContext) {
        return true;
    }
}

/**
 *
 * @param <I> - inputContext type
 */
class ByPassRequestMethodArgumentResolver<I> extends TypeSafedMethodArgumentResolver<I, I> {
    public ByPassRequestMethodArgumentResolver(Class<I> targetClass) {
        super(targetClass);
    }

    @Override
    protected I doResolve(MethodParameter parameter, I input) {
        return input;
    }
}

/**
 *
 * @param <I> - inputContext type
 */
class ByPassRequestHandlerResultHandler<I> extends TypeSafedHandlerResultHandler<I, I> {

    public ByPassRequestHandlerResultHandler(Class<I> targetClass) {
        super(targetClass);
    }

    @Override
    protected I doHandle(I returnValue) {
        return returnValue;
    }
}

/**
 *
 * @param <O> - outputContext type
 */
class VoidRequestHandlerResultHandler<O> extends TypeSafedHandlerResultHandler<O, Void> {

    private final Class<O> outputClass;

    public VoidRequestHandlerResultHandler(Class<O> outputClass) {
        super(Void.TYPE);
        this.outputClass = outputClass;
    }

    @Override
    protected O doHandle(Void returnValue) {
        return null;
    }
}