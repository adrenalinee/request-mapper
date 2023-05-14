package malibu.requestmapper;

import lombok.Getter;
import org.springframework.core.DefaultParameterNameDiscoverer;

import java.lang.reflect.Method;
import java.util.Objects;

@Getter
public final class HandlerMethod {

    private final Object bean;

    private final Method method;

    private final MethodParameter[] parameters;

    public HandlerMethod(Object bean, Method method) {
        Objects.requireNonNull(bean);
        Objects.requireNonNull(method);

        this.bean = bean;
        this.method = method;
        this.parameters = createMethodParameters(method);
    }

    private MethodParameter[] createMethodParameters(Method method) {
        int count = method.getParameterCount();
        final MethodParameter[] parameters = new MethodParameter[count];

        final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
        final String[] realParamNames = parameterNameDiscoverer.getParameterNames(method);
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = new MethodParameter(i, method, realParamNames[i]);
        }

        return parameters;
    }

    public String toString() {
        return new StringBuilder()
                .append("HandlerMethod[method=")
                .append(method)
                .append("]")
                .toString();
    }
}
