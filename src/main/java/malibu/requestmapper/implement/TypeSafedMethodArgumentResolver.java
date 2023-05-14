package malibu.requestmapper.implement;

import malibu.requestmapper.MethodParameter;
import malibu.requestmapper.RequestMethodArgumentResolver;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.reflect.TypeUtils;

import java.lang.reflect.Type;

/**
 *
 * @param <I> - inputContext type
 * @param <T> - argument type
 */
@RequiredArgsConstructor
public abstract class TypeSafedMethodArgumentResolver<I, T> implements RequestMethodArgumentResolver<I> {

    @NonNull
    private final Type targetType;

    @Override
    public final boolean supportsParameter(MethodParameter parameter) {
        return TypeUtils.isAssignable(parameter.getParameterType(), targetType);
    }

    @Override
    public final Object resolveArgument(MethodParameter parameter, I inputContext) {
        return doResolve(parameter, inputContext);
    }

    protected abstract T doResolve(MethodParameter parameter, I inputContext);
}
