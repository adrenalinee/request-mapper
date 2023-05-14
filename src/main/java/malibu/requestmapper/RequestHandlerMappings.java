package malibu.requestmapper;

import malibu.requestmapper.annotation.RequestMapping;
import malibu.requestmapper.exception.AmbiguousMatchedRequestFoundException;
import malibu.requestmapper.exception.MatchedRequestNotFoundException;
import malibu.requestmapper.util.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @param <I> input context type
 */
@Slf4j
@Accessors(chain = true)
//@RequiredArgsConstructor
public final class RequestHandlerMappings<I> {

    private final Collection<Object> handlerObjects;

    private final RequestHandlerMappingRegistry<I> mappingRegistry = new RequestHandlerMappingRegistry<>();

    private final List<RequestMappingConditionCreator<I>> definedMappingConditionCreators = Lists.newArrayList();

    /**
     * inputContext 를 처리할 handler 를 못찾았을 경우 응답할 기본 handler
     * enableThrowMatchedHandlerNotFoundException == false 일때 사용됨
     */
    @Setter
    @Getter
    private HandlerMethod notMatchedHandlerMethod;

//    /**
//     * inputContext 를 처리할 handler 를 못찾았을 경우 exception을 던질지 말지 설정
//     * exception을 던지지 않는다면 notMatchedHandlerMethod 를 리턴한다.
//     */
//    @Setter
//    @Getter
//    private boolean enableThrowMatchedHandlerNotFoundException;

    /**
     * notMatchedHandlerMethod 기본 값 등록
     */
    public RequestHandlerMappings(Collection<Object> handlerObjects) {
        this.handlerObjects = handlerObjects;

//        try {
//            notMatchedHandlerMethod = new HandlerMethod(this, getClass().getMethod("notMatchedMapping"));
//        } catch (NoSuchMethodException e) {
//            throw new RuntimeException(e);
//        }
    }

    /**
     * notMatchedHandlerMethod 용 메서드
     */
    public void notMatchedMapping() {
        if (log.isDebugEnabled()) {
            log.debug("matched handler method not exist.");
        }
    }

    /**
     * request handler 를 찾아서 mappingRegistry에 등록
     */
    void detectRequestHandlerMethods() {
        if (log.isTraceEnabled()) {
            log.trace("start");
        }

        handlerObjects.forEach(requestHandlerObj ->
                Stream.of(requestHandlerObj.getClass().getMethods())
                      .filter(method -> isHandlerMethod(method))
                      .forEach(method -> registerHandlerMethod(requestHandlerObj, method)));

//        if (notMatchedHandlerMethod == null) {
//            Stream.of(NotMatchedHandler.class.getMethods())
//                  .filter(method -> method.getName().equals("notMatchedMapping"))
//                  .findFirst()
//                  .ifPresent(method -> notMatchedHandlerMethod = new HandlerMethod(new NotMatchedHandler(), method));
//        }
    }

    /**
     * TODO 한번 find 된 handler 를 cache 할 수 있는 방법 필요.
     * @param inputContext
     * @return
     */
    HandlerMethod findHandler(I inputContext) {
        final List<HandlerMethod> matches = Lists.newArrayList();
        for (RequestMappingInfo<I> mappingInfo : mappingRegistry.getMappings().keySet()) {
            if (mappingInfo.isMatchingCondition(inputContext)) {
                matches.add(mappingRegistry.getMappedHandlerMethod(mappingInfo));
            }
        }

        if (matches.isEmpty()) {
//            if (enableThrowMatchedHandlerNotFoundException) {
//                throw new MatchedHandlerNotFoundException("matched request handler not found. inputContext: " + inputContext);
//            }

            if (notMatchedHandlerMethod == null) {
                throw new MatchedRequestNotFoundException(inputContext);
            }

            if (log.isDebugEnabled()) {
                log.debug(
                    "matched handler method not found!! execute notMatchedHandlerMethod: {}",
                    notMatchedHandlerMethod
                );
            }

            return notMatchedHandlerMethod;
        }

        if (matches.size() > 1) {
            if (log.isDebugEnabled()) {
                log.debug("matches.size() > 1");
            }
            throw new AmbiguousMatchedRequestFoundException(inputContext);
        }

        return matches.get(0);
    }

    private static boolean isHandlerMethod(Method method) {
        final Annotation[] annotations = method.getAnnotations();
        if (annotations == null) {
            return false;
        }

        for (Annotation annotation: annotations) {
            if (annotation instanceof RequestMapping) {
                return true;
            }

            if (annotation.annotationType().getAnnotation(RequestMapping.class) != null) {
                return true;
            }
        }

        return false;
    }

    /**
     * request를 처리할 handler method를 직접 등록한다.
     * MappingConditionCreator 가 등록된 이휴에 호출해야 한다.
     * @param bean
     * @param method
     */
    public RequestHandlerMappings<I> registerHandlerMethod(Object bean, Method method) {
        Objects.requireNonNull(bean);
        Objects.requireNonNull(method);

        final HandlerMethod handlerMethod = new HandlerMethod(bean, method);
        final RequestMappingInfo<I> requestMappingInfo = new RequestMappingInfo<>();

        definedMappingConditionCreators.stream()
                                       .filter(defineConditionCreator -> defineConditionCreator.isRequire(handlerMethod))
                                       .map(requiredConditionCreator -> requiredConditionCreator.createCondition(handlerMethod))
                                       .forEach(requiredCondition -> requestMappingInfo.addCondition(requiredCondition));

        mappingRegistry.register(requestMappingInfo, handlerMethod);
        return this;
    }

    /**
     * request들을 handling 할 method를 포함하고 있는 class의 instance를 등록한다.
     * @param bean
     * @return
     */
    public RequestHandlerMappings<I> registerHandlerObject(Object bean) {
        Objects.requireNonNull(bean);

        handlerObjects.add(bean);
        return this;
    }

    /**
     *
     * @param mappingConditionCreator
     * @return
     */
    public RequestHandlerMappings<I> addMappingConditionCreator(RequestMappingConditionCreator<I> mappingConditionCreator) {
        Objects.requireNonNull(mappingConditionCreator);

        definedMappingConditionCreators.add(mappingConditionCreator);
        return this;
    }

    /**
     *
     * @return
     */
    public boolean isEmptyMappingConditionCreators() {
        return definedMappingConditionCreators.isEmpty();
    }

//    /**
//     *
//     */
//    @Slf4j
//    public static class NotMatchedHandler {
//
//        /**
//         * handler method 로 사용됨.
//         */
//        public void notMatchedMapping() {
//            if (log.isDebugEnabled()) {
//                log.debug("matched handler method not exist.");
//            }
//        }
//    }
}
