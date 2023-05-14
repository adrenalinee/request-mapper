package malibu.requestmapper;

/**
 * @param <I> - inputContext type
 */
public interface RequestMappingCondition<I> {

    boolean isMatchingCondition(I inputContext);
}
