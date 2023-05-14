package malibu.requestmapper;

import malibu.requestmapper.annotation.ExceptionMapping;
import malibu.requestmapper.util.Maps;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Accessors(chain = true)
@RequiredArgsConstructor
public final class ExceptionHandlerMappings {

    private final Collection<Object> handlerObjects;

    private final Map<Class<? extends Throwable>, HandlerMethod> exceptionHandlerMethods = Maps.newHashMap();

    void detectHandlerMethods() {
        if (log.isTraceEnabled()) {
            log.trace("start");
        }

        handlerObjects.forEach(handler ->
                Stream.of(handler.getClass()
                                 .getMethods())
                      .filter(method -> isHandlerMethod(method))
                      .forEach(method -> {
                          final ExceptionMapping exceptionMapping = method.getAnnotation(ExceptionMapping.class);
                          final Class<? extends Throwable>[] exceptionClasses = exceptionMapping.value();
                          for (Class<? extends Throwable> exceptionClass : exceptionClasses) {
                              registerExceptionHandlerMethod(exceptionClass, new HandlerMethod(handler, method));
                          }
                      }));
    }

    HandlerMethod findHandler(Class<? extends Throwable> exceptionClass) {
        if (exceptionHandlerMethods.containsKey(exceptionClass)) {
            return exceptionHandlerMethods.get(exceptionClass);
        }

        Class<?> parentExceptionClass = exceptionClass;
        while (! parentExceptionClass.equals(Throwable.class)) {
            if (exceptionHandlerMethods.containsKey(parentExceptionClass)) {
                return exceptionHandlerMethods.get(parentExceptionClass);
            }

            parentExceptionClass = parentExceptionClass.getSuperclass();
        }

        return null;
    }

    /**
     * request handler안에서 exception이 발생했을때 처리한 exception handler method를 등록한다.
     *
     * @param exceptionClass - handling 할 exception
     * @param handlerMethod - exception handler method 관련 정보
     */
    public ExceptionHandlerMappings registerExceptionHandlerMethod(Class<? extends Throwable> exceptionClass, HandlerMethod handlerMethod) {
        Objects.requireNonNull(exceptionClass);
        Objects.requireNonNull(handlerMethod);

        handlerMethod.getMethod().setAccessible(true);

        final HandlerMethod oldHandlerMethod = exceptionHandlerMethods.put(exceptionClass, handlerMethod);
        if (oldHandlerMethod != null && oldHandlerMethod.equals(handlerMethod)) {
            throw new IllegalStateException("Ambiguous @ExceptionMapping method mapped for [" +
                    exceptionClass + "]: {" + oldHandlerMethod + ", " + handlerMethod + "}");
        }

        if (log.isDebugEnabled()) {
            log.debug("Exception handler mapping - exception class: {}, handler method: {}",
                    exceptionClass,
                    handlerMethod.getMethod());
        }

        return this;
    }

    private static boolean isHandlerMethod(Method method) {
        return method.getAnnotation(ExceptionMapping.class) != null;
    }
}