package malibu.requestmapper;

import malibu.requestmapper.util.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @param <I> - inputContext type
 * @param <O> - outputContext type
 */
public final class InterceptorRegistry<I, O> {

    private final List<RequestHandlerInterceptor<I, O>> interceptors = Lists.newArrayList();

    public InterceptorRegistry<I, O> addInterceptor(RequestHandlerInterceptor<I, O> interceptor) {
        Objects.requireNonNull(interceptor);

        if (this.interceptors.contains(interceptor)) {
            throw new RuntimeException("이미 등록된 interceptor 입니다. interceptor: " + interceptor);
        }

        this.interceptors.add(interceptor);
        return this;
    }

    Collection<RequestHandlerInterceptor<I, O>> getInterceptors() {
        return Collections.unmodifiableList(interceptors);
//        return interceptors.stream().collect(Collectors.toList());
    }
}
