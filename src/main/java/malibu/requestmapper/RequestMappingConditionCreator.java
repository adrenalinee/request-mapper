package malibu.requestmapper;

/**
 * RequestMappingCondition 을 직접 생성해서 전달하게 하지 않고
 * Creator 를 통해 전달하는 이유는 isRequire 단계에서만 생성될 수 있는 값들을 저장해두었다가
 * RequestMappingCondition 생성할때 전달하기 위함임.
 *
 * mapping 마다 Condition 을 각각 생성해서 가지고 있어야 한다. 왜냐면 request 마다 Condition 의 조건값들이 다르기 때문임.
 * @param <I> - inputContext type
 */
public interface RequestMappingConditionCreator<I> {

    /**
     * handlerMethod 가 이(this) RequestMappingCondition 을 사용해야하는지 여부.
     * @param handlerMethod
     * @return
     */
    boolean isRequire(HandlerMethod handlerMethod);

    /**
     * RequestMappingCondition 생성
     * @param handlerMethod
     * @return
     */
    RequestMappingCondition<I> createCondition(HandlerMethod handlerMethod);
}
