package malibu.requestmapper;

import malibu.requestmapper.util.Lists;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @param <I> - inputContext type
 */
@Slf4j
@ToString
@EqualsAndHashCode
public final class RequestMappingInfo<I> {

    private final List<RequestMappingCondition<I>> mappingConditions = Lists.newArrayList();

    /**
     *
     * @param mappingCondition
     */
    public void addCondition(RequestMappingCondition<I> mappingCondition) {
        mappingConditions.add(mappingCondition);
    }

    List<RequestMappingCondition<I>> getMappingConditions() {
        return mappingConditions;
    }

    /**
     *
     * @return
     */
    public boolean isMatchingCondition(I inputContext) {
        if (mappingConditions.isEmpty()) {
            return false;
        }

         boolean isMatching = true;
         for (RequestMappingCondition<I> mappingCondition: mappingConditions) {
             try {
                 isMatching = mappingCondition.isMatchingCondition(inputContext);
             } catch (Exception e) {
                 if (log.isInfoEnabled()) {
                    log.info("WARNING: mappingCondition.isMatchingCondition() 에서 error 발생. mappingCondition: " +
                            mappingCondition, e);
                 }

                 return false;
             }

             if (!isMatching) {
                 break;
             }
         }

        return isMatching;
    }

}
