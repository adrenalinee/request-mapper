package malibu.requestmapper;

import malibu.requestmapper.exception.ResultRequestNotFoundException;
import malibu.requestmapper.util.FixedSizeCache;
import malibu.requestmapper.util.Lists;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Type;
import java.util.*;

/**
 * @param <O> - outputContext type
 */
@Slf4j
@Accessors(chain = true)
public final class RequestHandlerResultHandlers<O> {

    private final List<RequestHandlerResultHandler<O>> handlerResultHandlers = Lists.newArrayList();

    /**
     * TBD
     * Pair를 키로 사용할 수있을지 확인해야 함.
     * 기본적으로 RequestHandlerResult를 키로 사용하는것이 맞으나,
     * RequestHandlerResult 에 returnValue는 요청마다 계속 바뀌기 때문에 키로 사용이 어렵다. 그래서 returnType, HandlerMethod 만 가지고
     * 키를 만든다.
     */
    private final FixedSizeCache<Pair<Type, HandlerMethod>, RequestHandlerResultHandler<O>> handlerResultHandlerCache = new FixedSizeCache<>(100);

    /**
     * 처리할 ResultHandler 를 찾지 못했을때 최종적으로 처리할 ResultHandler.
     * fallbackResultHandler가 등록되어 있지 않으면 ResultHandlerNotFoundException 가 발생한다.
     */
    @Setter
    private RequestHandlerResultHandler<O> fallbackResultHandler; // = new FallbackResultHandler<>();

    /**
     *
     * @param handlerResultHandler
     */
    public RequestHandlerResultHandlers<O> addHandlerResultHandler(RequestHandlerResultHandler<O> handlerResultHandler) {
        Objects.requireNonNull(handlerResultHandler);

        handlerResultHandlers.add(handlerResultHandler);

        return this;
    }

    /**
     *
     * @param handlerResult
     * @return
     */
    RequestHandlerResultHandler<O> findResultHandler(RequestHandlerResult handlerResult) {
        Objects.requireNonNull(handlerResult);

        final Pair<Type, HandlerMethod> handlerMethodReturnTypePair = Pair.of(handlerResult.getReturnType(), handlerResult.getHandlerMethod());
        RequestHandlerResultHandler<O> handlerResultHandler = handlerResultHandlerCache.get(handlerMethodReturnTypePair);
        if (handlerResultHandler == null) {
            for (RequestHandlerResultHandler<O> resultHandler : handlerResultHandlers) {
                if (resultHandler.isSupports(handlerResult)) {
                    handlerResultHandler = resultHandler;
                    handlerResultHandlerCache.put(handlerMethodReturnTypePair, resultHandler);
                    break;
                }
            }
        }

        if (handlerResultHandler != null) {
            return handlerResultHandler;
        }


        if (log.isDebugEnabled()) {
            log.debug("warning. requestHandlerResultHandler not found. handlerResult: {}", handlerResult);
        }

        if (fallbackResultHandler == null) {
            throw new ResultRequestNotFoundException("requestHandlerResultHandler not found. handlerResult: " + handlerResult);
        }

        if (log.isDebugEnabled()) {
            log.debug("using fallbackResultHandler. fallbackResultHandler: {}", fallbackResultHandler);
        }
        return fallbackResultHandler;
    }

}