package malibu.requestmapper.implement;

import malibu.requestmapper.RequestHandlerResult;
import malibu.requestmapper.RequestHandlerResultHandler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.TypeUtils;

import java.lang.reflect.Type;

/**
 *
 * @param <O> - outputContext type
 * @param <T> - return value type
 */
@Slf4j
@RequiredArgsConstructor
public abstract class TypeSafedHandlerResultHandler<O, T> implements RequestHandlerResultHandler<O> {

    @NonNull
    private final Type targetType;

    @Override
    public final boolean isSupports(RequestHandlerResult handlerResult) {
        return TypeUtils.isAssignable(handlerResult.getReturnType(), targetType);
    }

    @Override
    public final O handleResult(RequestHandlerResult handlerResult) {
        // isSupports 함수에서 타입 체크가 끝난 상태임. 만약 ClassCasting exception 이 발생한다면 그냥 throw 되어야 함. 프레임워크가 오동작한 상황임.
        @SuppressWarnings("unchecked") final T typeCastedReturnValue = (T) handlerResult.getReturnValue();

        return doHandle(typeCastedReturnValue);
    }

    protected abstract O doHandle(T returnValue);
}
