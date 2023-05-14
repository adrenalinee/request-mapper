package malibu.requestmapper;

import malibu.requestmapper.exception.*;
import malibu.requestmapper.util.FixedSizeCache;
import malibu.requestmapper.util.Lists;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.springframework.util.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 *
 * @param <I> - inputContext type
 */
@Slf4j
public final class RequestHandlerMethodInvoker<I> {

    private static final Object[] EMPTY_ARRAY = new Object[0];

    private final List<RequestMethodArgumentResolver<I>> argumentResolvers = Lists.newArrayList();

    @Getter(AccessLevel.PACKAGE) //for test...
    private final FixedSizeCache<MethodParameter, RequestMethodArgumentResolver<I>> argumentResolverCache = new FixedSizeCache<>(100);

    @Getter(AccessLevel.PACKAGE)
    private final ExceptionHandlerMappings exceptionHandlerMappings;

    /**
     * argument resolving 중에 에러가 발생하면 핸들러 처리를 하지 않고 에러를 발생시킬지 여부
     *  true: {@link RequestMethodArgResolveFailException} 을 발생시킴.
     *  false: 경고 로그 메시지만 출력하고 argument는 null 로 처리 됨.
     */
//    @Getter
//    @Setter
    private boolean enableThrowArgumentResolveFailException = false;

    public RequestHandlerMethodInvoker(Collection<Object> handlerObjects) {
        exceptionHandlerMappings = new ExceptionHandlerMappings(handlerObjects);
    }

    void detectExceptionHandlerMethods() {
        exceptionHandlerMappings.detectHandlerMethods();
    }

    /**
     *
     * @param argumentResolvers
     */
    public RequestHandlerMethodInvoker<I> addMethodArgumentResolver(RequestMethodArgumentResolver<I> argumentResolvers) {
        Objects.requireNonNull(argumentResolvers);

        this.argumentResolvers.add(argumentResolvers);

        return this;
    }

    RequestHandlerResult invoke(I inputContext, HandlerMethod handlerMethod) throws Throwable {
        if (log.isTraceEnabled()) {
            log.trace("start");
        }

        final Object[] args = resolveArguments(inputContext, handlerMethod, null);

        try {
            final Object result = doInvoke(handlerMethod.getBean(), handlerMethod.getMethod(), args);
            return new RequestHandlerResult(result, handlerMethod.getMethod().getGenericReturnType(), handlerMethod);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new RequestMethodInvokeFailException("can not invoke handler method.", e);
        } catch (InvocationTargetException e) {
            throw e.getCause();
//            return exceptionHandlerInvoke(inputContext, e.getCause(), handlerMethod);
        }
    }

    RequestHandlerResult exceptionHandlerInvoke(I inputContext, Throwable throwedException, HandlerMethod handlerMethod) {
        final HandlerMethod exceptionHandlerMethod = exceptionHandlerMappings.findHandler(throwedException.getClass());

        if (exceptionHandlerMethod == null) { //발생한 exception을 처리할 handler가 없을 경우 request handler가 최종 에러난 것으로 처리
            if (log.isDebugEnabled()) {
                log.debug("WARNING: throw error request handler mathod.", throwedException);
            }

            throw new RequestExecutionException("throw error request handler execution. {request handler: " +
                                                           handlerMethod +
                                                           ", rised exception: " +
                                                           throwedException +
                                                           "}", throwedException)
                    .setHandlerMethod(handlerMethod);
        }

        if (log.isTraceEnabled()) {
            log.trace("execute exception handler.");
        }
        final Object[] args = resolveArguments(inputContext, exceptionHandlerMethod, throwedException);

        try {
            final Object result = doInvoke(exceptionHandlerMethod.getBean(), exceptionHandlerMethod.getMethod(), args);
            return new RequestHandlerResult(result, exceptionHandlerMethod.getMethod().getGenericReturnType(), exceptionHandlerMethod);
        } catch (InvocationTargetException | IllegalAccessException e) {
            final Throwable risedThrowable = e.getCause();
            if (log.isDebugEnabled()) {
                log.debug("WARNING: throw error exception handler mathod.", risedThrowable);
            }

            throw new ExceptionRequestException("throw error exception handler mathod. {exception handler: " +
                                                                    exceptionHandlerMethod +
                                                                    ", rised exception: " +
                                                                    risedThrowable +
                                                                    "}", risedThrowable)
                    .setHandlerMethod(exceptionHandlerMethod)
                    .setRisedThrowable(risedThrowable)
                    .setHandlerThrowable(throwedException);
        }
    }

    private Object doInvoke(Object bean, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        if (log.isTraceEnabled()) {
            log.trace("Invoking '{}' with arguments {}", ClassUtils.getQualifiedMethodName(method, bean.getClass()), Arrays.toString(args));
        }

        final Object result = method.invoke(bean, args);
        if (log.isTraceEnabled()) {
            log.trace("Method [{}] returned [{}]", ClassUtils.getQualifiedMethodName(method, bean.getClass()), result);
        }
        return result;
    }

    private Object[] resolveArguments(I inputContext, HandlerMethod handlerMethod, Throwable throwedException) {
        final MethodParameter[] parameters = handlerMethod.getParameters();
        if (parameters == null || parameters.length == 0) {
            return EMPTY_ARRAY;
        }

        final Object[] resolvedValues = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) { //method argument 별로 resolver를 찾아서 전달할 값을 resolving한다.
            final MethodParameter parameter = parameters[i];

            if (throwedException != null) {
//                if (parameter.getParameterType().isAssignableFrom(throwedException.getClass())) {
//                if (TypeUtils.isAssignable(parameter.getParameterType(), throwedException.getClass())) {
                if (TypeUtils.isAssignable(throwedException.getClass(), parameter.getParameterType())) {
                    resolvedValues[i] = throwedException;
                    continue;
                }
            }

            final RequestMethodArgumentResolver<I> argumentResolver = findArgumentResolver(parameter);
            if (argumentResolver == null) {
                throw new RequestMethodArgResolverNotFoundException(handlerMethod.getMethod(), parameter);
            }

            resolvedValues[i] = resolveArgument(inputContext, argumentResolver, parameter);
        }

        return resolvedValues;
    }

    /**
     * method argument resolver 를 찾는다. cache 에서 먼저 찾는다.
     *
     * @param parameter
     * @return
     */
    RequestMethodArgumentResolver<I> findArgumentResolver(MethodParameter parameter) {
        RequestMethodArgumentResolver<I> findedResolver = argumentResolverCache.get(parameter);
        if (findedResolver == null) {
            for (RequestMethodArgumentResolver<I> resolver: argumentResolvers) {
                if (resolver.supportsParameter(parameter)) {
                    findedResolver = resolver;
                    argumentResolverCache.put(parameter, findedResolver);
                    break;
                }
            }
        }

        return findedResolver;
    }

    /**
     * method argument resolver에서 argument값을 resolving한다. resolver를 못찾고, argument가 primitive type일 경우에는 기본값을 전달한다.
     *
     * @param inputContext
     * @param argumentResolver
     * @param methodParameter
     * @return
     */
    Object resolveArgument(I inputContext, RequestMethodArgumentResolver<I> argumentResolver, MethodParameter methodParameter) {
        Object resolvedValue = null;
        try {
            resolvedValue = argumentResolver.resolveArgument(methodParameter, inputContext);
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("WARNING: argumentResolver.resolveArgument()에서 에러 발생. argumentResolver: " +
                        argumentResolver, e);
            }

            if (enableThrowArgumentResolveFailException) {
                throw new RequestMethodArgResolveFailException(e);
            }
        }

        if (resolvedValue == null) { //argument가 primitive type일 경우에는 기본값이 꼭 있어야 한다.
            final Class<?> parameterType = methodParameter.getParameter().getType();
            if (parameterType.isPrimitive()) {
                if (parameterType.equals(boolean.class)) {
                    resolvedValue = false;
                } else if (parameterType.equals(int.class)) {
                    resolvedValue = 0;
                } else if (parameterType.equals(long.class)) {
                    resolvedValue = 0L;
                } else if (parameterType.equals(float.class)) {
                    resolvedValue = 0F;
                } else if (parameterType.equals(double.class)) {
                    resolvedValue = 0D;
                } else if (parameterType.equals(byte.class)) {
                    resolvedValue = (byte) 0;
                } else if (parameterType.equals(short.class)) {
                    resolvedValue = (short) 0;
                } else if (parameterType.equals(char.class)) {
                    resolvedValue = (char) 0;
                }
            }
        }

        return resolvedValue;
    }

    /**
     * argument resolving 중에 에러가 발생하면 핸들러 처리를 하지 않고 에러를 발생시킬지 여부
     *  true: {@link RequestMethodArgResolveFailException} 을 발생시킴.
     *  false: 경고 로그 메시지만 출력하고 argument는 null 로 처리 됨.
     */
    public boolean enableThrowArgumentResolveFailException() {
        return this.enableThrowArgumentResolveFailException;
    }

    /**
     * argument resolving 중에 에러가 발생하면 핸들러 처리를 하지 않고 에러를 발생시킬지 여부
     *  true: {@link RequestMethodArgResolveFailException} 을 발생시킴.
     *  false: 경고 로그 메시지만 출력하고 argument는 null 로 처리 됨.
     */
    public RequestHandlerMethodInvoker<I> enableThrowArgumentResolveFailException(boolean flag) {
        this.enableThrowArgumentResolveFailException = flag;

        return this;
    }

    public static void main(String[] args) {
        System.out.println(
                TypeUtils.isAssignable(NullPointerException.class, Exception.class)
        );
    }
}
