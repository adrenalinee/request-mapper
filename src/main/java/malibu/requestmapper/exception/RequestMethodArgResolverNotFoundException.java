package malibu.requestmapper.exception;

import malibu.requestmapper.MethodParameter;

import java.lang.reflect.Method;

public class RequestMethodArgResolverNotFoundException extends RequestMapperException {

    public RequestMethodArgResolverNotFoundException(Method method, MethodParameter parameter) {
        super("No suitable resolver for argument " +
                parameter.getParameterIndex() +
                " of type '" +
                parameter.getParameterType().getTypeName() +
                "' on " +
                method.toGenericString());
    }
}
