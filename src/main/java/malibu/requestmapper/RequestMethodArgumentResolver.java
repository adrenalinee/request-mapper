package malibu.requestmapper;

/**
 * @param <I> - inputContext type
 */
public interface RequestMethodArgumentResolver<I> {

    boolean supportsParameter(MethodParameter parameter);

    Object resolveArgument(MethodParameter parameter, I inputContext);
}
