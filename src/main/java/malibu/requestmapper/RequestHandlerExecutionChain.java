package malibu.requestmapper;


import malibu.requestmapper.util.Lists;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @param <I> - inputContext type
 * @param <O> - outputContext type
 */
@Slf4j
public final class RequestHandlerExecutionChain<I, O> {

    @Getter
    private final HandlerMethod handlerMethod;

    private final List<RequestHandlerInterceptor<I, O>> matchedInterceptors = Lists.newArrayList();

    private RequestHandlerInterceptor<I, O>[] cachedMatchedInterceptors;

    private int interceptorIndex = -1;

    public RequestHandlerExecutionChain(HandlerMethod handlerMethod) {
        this.handlerMethod = handlerMethod;
    }

    public void addInterceptor(RequestHandlerInterceptor<I, O> interceptor) {
        matchedInterceptors.add(interceptor);
    }

    public boolean applyPreHandle(I inputContext) {
        final RequestHandlerInterceptor<I, O>[] interceptors = getInterceptors();
        if (!(interceptors == null || interceptors.length == 0)) {
            for (int i = 0; i < interceptors.length; i++) {
                final RequestHandlerInterceptor<I, O> interceptor = interceptors[i];
                if (!interceptor.preHandle(inputContext, handlerMethod)) {
                    return false;
                }

                interceptorIndex = i;
            }
        }

        return true;
    }

    public void applyPostHandle(I inputContext, Object returnValue) {
        final RequestHandlerInterceptor<I, O>[] interceptors = getInterceptors();
        if (!(interceptors == null || interceptors.length == 0)) {
            for (int i = interceptors.length - 1; i >= 0; i--) {
                final RequestHandlerInterceptor<I, O> interceptor = interceptors[i];
                interceptor.postHandle(inputContext, handlerMethod, returnValue);
            }
        }
    }

    public void applyAfterCompletion(I inputContext, O outputContext, Throwable ex) {
        final RequestHandlerInterceptor<I, O>[] interceptors = getInterceptors();
        if (!(interceptors == null || interceptors.length == 0)) {
            for (int i = interceptorIndex; i >= 0; i--) {
                final RequestHandlerInterceptor<I, O> interceptor = interceptors[i];

                try {
                    interceptor.afterCompletion(inputContext, handlerMethod, outputContext, ex);
                } catch (Throwable throwable) {
                    log.warn("WARNING: throw error RequestHandlerInterceptor.afterCompletion()", throwable);
                }
            }
        }
    }

    private RequestHandlerInterceptor<I, O>[] getInterceptors() {
        if (cachedMatchedInterceptors == null && !matchedInterceptors.isEmpty()) {
            cachedMatchedInterceptors = matchedInterceptors.toArray(new RequestHandlerInterceptor[matchedInterceptors.size()]);
        }

        return cachedMatchedInterceptors;
    }
}
