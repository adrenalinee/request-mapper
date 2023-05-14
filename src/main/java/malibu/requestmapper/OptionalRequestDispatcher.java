package malibu.requestmapper;

import malibu.requestmapper.exception.RequestExecutionException;
import malibu.requestmapper.exception.RequestMethodInvokeFailException;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * servlet dispatcher
 *
 * @param <I> - inputContext type
 * @param <O> - outputContext type
 */
@Slf4j
public final class OptionalRequestDispatcher<I, O> extends AbstractRequestDispatcher<I, O> {

//    public OptionalRequestDispatcher(Class<I> inputClass, Class<O> outputClass) {
//        super(inputClass, outputClass);
//    }

    /**
     *
     * @param inputContext
     * @return
     * @throws RequestExecutionException
     * @throws RequestMethodInvokeFailException
     */
    public Optional<O> handle(I inputContext) {
        if (log.isTraceEnabled()) {
            log.trace("start");
        }

        final HandlerMethod handlerMethod = handlerMappings.findHandler(inputContext);
        final RequestHandlerExecutionChain<I, O> handlerExecutionChain = createHandlerExecutionChain(handlerMethod);

        O outputContext = null;
        Throwable handlerException = null;
        try {
            RequestHandlerResult handlerResult = null;

            try {
                if (!handlerExecutionChain.applyPreHandle(inputContext)) {
                    return Optional.empty();
                }

                handlerResult = handlerMethodInvoker.invoke(inputContext, handlerMethod);

                handlerExecutionChain.applyPostHandle(inputContext, handlerResult.getReturnValue());
            } catch (Throwable ex) { //handler error!
                handlerException = ex;
            }

            if (handlerException != null) {
                handlerResult = handlerMethodInvoker.exceptionHandlerInvoke(inputContext, handlerException, handlerMethod);
                handlerException = null;
            }

            final RequestHandlerResultHandler<O> resultHandler = handlerResultHandlers.findResultHandler(handlerResult);
            outputContext = resultHandler.handleResult(handlerResult);
        } finally {
            handlerExecutionChain.applyAfterCompletion(inputContext, outputContext, handlerException);
        }

        return Optional.ofNullable(outputContext);
    }
}