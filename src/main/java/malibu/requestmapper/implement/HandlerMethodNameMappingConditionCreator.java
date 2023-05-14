package malibu.requestmapper.implement;

import malibu.requestmapper.HandlerMethod;
import malibu.requestmapper.RequestMappingCondition;
import malibu.requestmapper.RequestMappingConditionCreator;
import malibu.requestmapper.annotation.RequestMapping;
import lombok.RequiredArgsConstructor;

/**
 *
 */
public class HandlerMethodNameMappingConditionCreator
    implements RequestMappingConditionCreator<String> {
    @Override
    public boolean isRequire(HandlerMethod handlerMethod) {
        return handlerMethod.getMethod().getAnnotation(RequestMapping.class) != null;
    }

    @Override
    public RequestMappingCondition<String> createCondition(HandlerMethod handlerMethod) {
        return new HandlerMethodNameMappingCondition(handlerMethod.getMethod().getName());
    }
}

/**
 *
 */
@RequiredArgsConstructor
class HandlerMethodNameMappingCondition
    implements RequestMappingCondition<String> {

    private final String handlerMethodName;

    @Override
    public boolean isMatchingCondition(String inputContext) {
        return handlerMethodName.equals(inputContext);
    }
}