package malibu.requestmapper;

import malibu.requestmapper.exception.RequestMethodArgResolveFailException;
import malibu.requestmapper.impl.TestRequest;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class RequestHandlerMethodInvokerTest {

    public void narmalNoArguHandler() {

    }

    public String normalOneArguHandler(String name) {
        return name;
    }

    public void testRequestHandler() {
        throw new TestException();
    }

    public void testNullpointerRequestHandler() {
        throw new NullPointerException();
    }

    public String exceptionHandler(Exception e) {
        return e.getMessage();
    }

    public String testExceptionHandler(TestException e) {
        return e.getMessage();
    }

    public void testExceptionHandler2() throws Exception {
        throw new Exception();
    }

    @Test
    @DisplayName("argument가 없고 리턴도 없는 handler method를 잘 실행하는지 확인")
    public void invokeNoArguTest() throws Throwable {
        final Method requestMethod = getClass().getMethod("narmalNoArguHandler");

        final RequestHandlerMethodInvoker<TestRequest> requestRequestHandlerMethodInvoker =
                new RequestHandlerMethodInvoker<>(Collections.emptyList());

        final HandlerMethod requestHandlerMethod =  new HandlerMethod(this, requestMethod);
        final RequestHandlerResult requestHandlerResult =
                requestRequestHandlerMethodInvoker.invoke(new TestRequest(), requestHandlerMethod); //test!

        assertNotNull(requestHandlerResult);
        assertNull(requestHandlerResult.getReturnValue());

        assertNotNull(requestHandlerResult.getHandlerMethod());
        assertEquals(requestHandlerResult.getHandlerMethod(), requestHandlerMethod);
    }

    @Test
    @DisplayName("argument를 가진 평범한 handler method를 잘 실행하는지 확인")
    public void invokeOneArguTest() throws Throwable {
        final String name = "QWERTY";

        final Method requestMethod = getClass().getMethod("normalOneArguHandler", String.class);



        final RequestHandlerMethodInvoker<TestRequest> requestRequestHandlerMethodInvoker =
                new RequestHandlerMethodInvoker<>(Collections.emptyList());

        requestRequestHandlerMethodInvoker.addMethodArgumentResolver(new StringArgumentResolver());

        final TestRequest testRequest = new TestRequest();
        testRequest.setName(name);

        final HandlerMethod requestHandlerMethod =  new HandlerMethod(this, requestMethod);
        assertNull(requestRequestHandlerMethodInvoker.getArgumentResolverCache()
                                                     .get(requestHandlerMethod.getParameters()[0])); //caching이 안되어 있는지 확인!

        final RequestHandlerResult requestHandlerResult =
                requestRequestHandlerMethodInvoker.invoke(testRequest, requestHandlerMethod); //test!

        assertNotNull(requestHandlerResult);
        assertNotNull(requestHandlerResult.getReturnValue());
        assertEquals(requestHandlerResult.getReturnValue(), name);

        assertNotNull(requestHandlerResult.getHandlerMethod());
        assertEquals(requestHandlerResult.getHandlerMethod(), requestHandlerMethod);

        final RequestMethodArgumentResolver<TestRequest> argumentResolver =
                requestRequestHandlerMethodInvoker.getArgumentResolverCache()
                                                  .get(requestHandlerMethod.getParameters()[0]);

        assertNotNull(argumentResolver); //caching이 되었는지 확인!
        assertTrue(argumentResolver instanceof StringArgumentResolver);
    }

    @Test
    @DisplayName("request handler에서 exception이 발생했을때 해당 exception을 throw 하는지 확인")
    public void invokeWithThrowErrorTest() throws Throwable {
        final Method requestMethod = getClass().getMethod("testRequestHandler");
        final Method exceptionMethod = getClass().getMethod("testExceptionHandler", TestException.class);

        final RequestHandlerMethodInvoker<TestRequest> requestRequestHandlerMethodInvoker =
                new RequestHandlerMethodInvoker<>(Collections.emptyList());

        final HandlerMethod exceptionHandlerMethod = new HandlerMethod(this, exceptionMethod);
        requestRequestHandlerMethodInvoker.getExceptionHandlerMappings()
                                          .registerExceptionHandlerMethod(TestException.class, exceptionHandlerMethod);


        assertThrows(TestException.class, () -> requestRequestHandlerMethodInvoker.invoke(new TestRequest(), new HandlerMethod(this, requestMethod)));

//        final RequestHandlerResult requestHandlerResult =
//                requestRequestHandlerMethodInvoker.invoke(new TestRequest(), new HandlerMethod(this, requestMethod)); //test!
//
//        assertNotNull(requestHandlerResult);
//
//        assertNotNull(requestHandlerResult.getReturnValue());
//        assertEquals(requestHandlerResult.getReturnValue(), TestException.RETURN_MESSAGE);
//
//        assertNotNull(requestHandlerResult.getHandlerMethod());
//        assertEquals(requestHandlerResult.getHandlerMethod(), exceptionHandlerMethod);
    }

//    @Test
//    @DisplayName("exception handler에서 exception 발생시 관련 에러가 throw 되는지 확인")
//    public void invokeWithThrowErrorTest2() throws NoSuchMethodException {
//        final Method requestMethod = getClass().getMethod("testRequestHandler");
//        final Method exceptionMethod = getClass().getMethod("testExceptionHandler2");
//
//        final RequestHandlerMethodInvoker<TestRequest> requestRequestHandlerMethodInvoker =
//                new RequestHandlerMethodInvoker<>(Collections.emptyList());
//
//        final HandlerMethod exceptionHandlerMethod = new HandlerMethod(this, exceptionMethod);
//        requestRequestHandlerMethodInvoker.getExceptionHandlerMappings()
//                                          .registerExceptionHandlerMethod(TestException.class, exceptionHandlerMethod);
//
//        final ExceptionHandlerMethodInvokeException resultError =
//            assertThrows(ExceptionHandlerMethodInvokeException.class, () ->
//                    requestRequestHandlerMethodInvoker.invoke(new TestRequest(), new HandlerMethod(this, requestMethod))); //test!
//
//        assertEquals(resultError.getRisedThrowable().getClass(), Exception.class);
//        assertEquals(resultError.getHandlerMethod(), exceptionHandlerMethod);
//    }

//    @Test
//    @DisplayName("exception handler에서 exception 발생시 상위(spuer) exception 핸들러가 처리하는지 확인")
//    public void invokeWithThrowErrorTest2_1() throws Throwable {
//        final Method requestMethod = getClass().getMethod("testNullpointerRequestHandler");
//        final Method exceptionMethod = getClass().getMethod("exceptionHandler", Exception.class);
//
//        final RequestHandlerMethodInvoker<TestRequest> requestRequestHandlerMethodInvoker =
//                new RequestHandlerMethodInvoker<>(Collections.emptyList());
//
//        final HandlerMethod exceptionHandlerMethod = new HandlerMethod(this, exceptionMethod);
//        requestRequestHandlerMethodInvoker.getExceptionHandlerMappings()
//                                          .registerExceptionHandlerMethod(Exception.class, exceptionHandlerMethod);
//
//        final RequestHandlerResult requestHandlerResult =
//                requestRequestHandlerMethodInvoker.invoke(new TestRequest(), new HandlerMethod(this, requestMethod)); //test!
//
//        assertNotNull(requestHandlerResult);
//
//        assertNotNull(requestHandlerResult.getHandlerMethod());
//        assertEquals(requestHandlerResult.getHandlerMethod(), exceptionHandlerMethod);
//    }

//    @Test
//    @DisplayName("exception handler 등록이 안되어 있을 경우 에러가 throw 되는지 확인")
//    public void invokeWithThrowErrorTest3() throws Throwable {
//        final Method requestMethod = getClass().getMethod("testRequestHandler");
//
//        final RequestHandlerMethodInvoker<TestRequest> requestRequestHandlerMethodInvoker =
//                new RequestHandlerMethodInvoker<>(Collections.emptyList());
//
//        assertThrows(HandlerExecutionException.class, () ->
//                requestRequestHandlerMethodInvoker.invoke(new TestRequest(), new HandlerMethod(this, requestMethod))); //test!
//    }
}

class TestException extends RuntimeException {
    public static final String RETURN_MESSAGE = "TestException thrown!!";

    public TestException() {
        super(RETURN_MESSAGE);
    }
}

class StringArgumentResolver implements RequestMethodArgumentResolver<TestRequest> {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
//        return String.class.isAssignableFrom(parameter.getParameterType());
        return TypeUtils.isAssignable(String.class, parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, TestRequest inputContext) throws RequestMethodArgResolveFailException {
        return inputContext.getName();
    }
}