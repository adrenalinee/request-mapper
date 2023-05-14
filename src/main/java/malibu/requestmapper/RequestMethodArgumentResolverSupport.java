package malibu.requestmapper;

import malibu.requestmapper.exception.RequestMethodArgResolverNotFoundException;
import malibu.requestmapper.exception.TypeMissMatchException;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @param <I> - inputContext type
 */
@Slf4j
public abstract class RequestMethodArgumentResolverSupport<I> implements RequestMethodArgumentResolver<I> {

    private final TypeConverterRegistry typeConverterRegistry = TypeConverterRegistry.getInstance();

    private final RequestHandlerMethodInvoker<I> requestHandlerMethodInvoker = new RequestHandlerMethodInvoker<>(null);


    /**
     * TBD
     */
    public Object resolve(MethodParameter methodParameter, I inputContext) {
        final RequestMethodArgumentResolver<I> argumentResolver = requestHandlerMethodInvoker.findArgumentResolver(methodParameter);
        if (argumentResolver == null) {
            throw new RequestMethodArgResolverNotFoundException(null, methodParameter); //TODO null 은 넘기면 안됨!
        }

        return requestHandlerMethodInvoker.resolveArgument(inputContext, argumentResolver, methodParameter);
    }

    /**
     * 원하는 타입으로 바꿀 수 있는 converter 가 등록되어 있는지 확인한다.
     */
    protected boolean isConvertable(Class<?> parameterTypeClass) {
        Objects.requireNonNull(parameterTypeClass);

        return typeConverterRegistry.exist(parameterTypeClass);
    }

    /**
     * 알맞은 converter 를 찾아서 converting 해준다.
     */
    protected <T> T convert(String value, Class<T> parameterTypeClass) {
        Objects.requireNonNull(parameterTypeClass);

        if (value == null) {
            return null;
        }

        final TypeConverter<T> typeConverter = typeConverterRegistry.find(parameterTypeClass);
        if (typeConverter == null) {
            //TODO
            log.warn("typeConverter not found. requiredType: {}", parameterTypeClass);

            return null;
        }

        try {
            return typeConverter.convert(value, parameterTypeClass);
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("typeConverter.convert()에서 에러가 발생했습니다. value: {}, parameterTypeClass: {}", value, parameterTypeClass);
            }

            throw new TypeMissMatchException(e);
        }
    }

}
