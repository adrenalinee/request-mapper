package malibu.requestmapper;

import malibu.requestmapper.exception.ExceptionRequestException;
import malibu.requestmapper.exception.RequestExecutionException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * reactive dispatcher
 *
 * @param <I> - inputContext type
 * @param <O> - outputContext type
 */
@Slf4j
public final class MonoRequestDispatcher<I, O> extends AbstractRequestDispatcher<I, O> {

//    public MonoRequestDispatcher(Class<I> inputClass, Class<O> outputClass) {
//        super(inputClass, outputClass);
//    }

    /**
     * @return
     */
    public Mono<O> handle(I inputContext) {
        if (log.isTraceEnabled()) {
            log.trace("start");
        }

        final HandlerMethod handlerMethod = handlerMappings.findHandler(inputContext);
        final RequestHandlerExecutionChain<I, O> handlerExecutionChain = createHandlerExecutionChain(handlerMethod);

        Mono<?> resultMono = null;
        try {
            if (!handlerExecutionChain.applyPreHandle(inputContext)) {
                handlerExecutionChain.applyAfterCompletion(inputContext, null, null);
                return Mono.empty();
            }
        } catch (Throwable ex) {
            resultMono = Mono.error(ex);
        }

        if (resultMono == null) {
            RequestHandlerResult handlerResult;
            try {
                handlerResult = handlerMethodInvoker.invoke(inputContext, handlerMethod);
            } catch (Throwable ex) {
                final Mono<O> monoError = Mono.error(ex);
                handlerResult = new RequestHandlerResult(Mono.error(ex), monoError.getClass(), handlerMethod);
            }

            if (! handlerResult.getReturnType()
                              .getTypeName()
                              .startsWith(Mono.class.getName())) {

                resultMono = Mono.justOrEmpty(handlerResult.getReturnValue());
            } else {
                resultMono = (Mono<?>) handlerResult.getReturnValue();
            }
        }

        return resultMono.doOnNext(returnValue -> handlerExecutionChain.applyPostHandle(inputContext, returnValue))
                         .map(returnValue -> new RequestHandlerResult(returnValue, unwrapReturnType(handlerMethod), handlerMethod))
                         .onErrorResume(handlerThrowable -> { //mono 로 에러가 전달되었을 경우 exception handler 를 찾아서 처리함.
                             final RequestHandlerResult exceptionHandlerResult = handlerMethodInvoker.exceptionHandlerInvoke(inputContext, handlerThrowable, handlerMethod);
                             if (exceptionHandlerResult.getReturnValue() == null ||
                                 ! Mono.class.isAssignableFrom(exceptionHandlerResult.getReturnValue().getClass())) {

                                 return Mono.just(exceptionHandlerResult);
                             }

                             final HandlerMethod handlerMethodForReturnMono = exceptionHandlerResult.getHandlerMethod();
                             final Mono<?> exceptionHandlerReturnMono = (Mono<?>) exceptionHandlerResult.getReturnValue();
                             return exceptionHandlerReturnMono.map(returnValue -> new RequestHandlerResult(returnValue, unwrapReturnType(handlerMethodForReturnMono), handlerMethodForReturnMono));
                         })
                         .map(requestHandlerResult -> handlerResultHandlers.findResultHandler(requestHandlerResult)
                                                                           .handleResult(requestHandlerResult))
                         .doOnError(ex -> {
                             if (ex instanceof RequestExecutionException || ex instanceof ExceptionRequestException) {
                                 handlerExecutionChain.applyAfterCompletion(inputContext, null, ex.getCause());
                             } else {
                                 handlerExecutionChain.applyAfterCompletion(inputContext, null, ex);
                             }
                         })
                         .doOnSuccess(output -> handlerExecutionChain.applyAfterCompletion(inputContext, output, null))

                ;
    }

    private Type unwrapReturnType(HandlerMethod handlerMethod) {
        final Type returnType = handlerMethod.getMethod().getGenericReturnType();
        if (returnType instanceof ParameterizedType) {
            final ParameterizedType wrappedReturnType = (ParameterizedType) returnType;
            return wrappedReturnType.getActualTypeArguments()[0];
        }

        return returnType;
    }

}
