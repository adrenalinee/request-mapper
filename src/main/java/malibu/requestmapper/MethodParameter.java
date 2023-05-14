package malibu.requestmapper;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

@ToString
@Getter
@EqualsAndHashCode
public final class MethodParameter {

    private final int parameterIndex;

    private volatile Parameter parameter;

    private volatile Type parameterType;

    private volatile Annotation[] parameterAnnotations;

    private volatile String parameterName;

    public MethodParameter(int parameterIndex, Method method, String realName) {
        this.parameterIndex = parameterIndex;

        this.parameter = method.getParameters()[parameterIndex];
        this.parameterType = method.getParameterTypes()[parameterIndex];
        this.parameterAnnotations = method.getParameterAnnotations()[parameterIndex];
        this.parameterName = realName;
    }
}
