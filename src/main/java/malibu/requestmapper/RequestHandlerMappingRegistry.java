package malibu.requestmapper;

import malibu.requestmapper.util.Maps;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;

@Slf4j
public final class RequestHandlerMappingRegistry<I> {

    private final Map<RequestMappingInfo<I>, HandlerMethod> registry = Maps.newHashMap();

    private Map<RequestMappingInfo<I>, HandlerMethod> immutableRegistry;

    /**
     *
     * @param requestMappingInfo
     * @param handlerMethod
     */
    public void register(RequestMappingInfo<I> requestMappingInfo, HandlerMethod handlerMethod) {
        handlerMethod.getMethod().setAccessible(true);

        if (log.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            requestMappingInfo.getMappingConditions()
                .forEach(c -> sb.append(c).append("; "));
            if (sb.length() == 0) {
                sb.append("[WARN: not found mappingConditions!!]");
            }

            log.info("Request handler mapping - conditions: {},  handler: {}",
                    sb,
                    handlerMethod.getMethod());
        }
        registry.put(requestMappingInfo, handlerMethod);

        if (immutableRegistry != null) {
//            immutableRegistry.clear();
            immutableRegistry = null;
        }
    }

    /**
     * register가 끝나기 전에 호출하면 좋지 않음.
     * @return
     */
    Map<RequestMappingInfo<I>, HandlerMethod> getMappings() {
        if (immutableRegistry == null) {
            immutableRegistry = Collections.unmodifiableMap(registry);
        }

        return immutableRegistry;
    }

    public HandlerMethod getMappedHandlerMethod(RequestMappingInfo<I> requestMappingInfo) {
        return registry.get(requestMappingInfo);
    }
}
