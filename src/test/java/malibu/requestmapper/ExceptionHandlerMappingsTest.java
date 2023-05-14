package malibu.requestmapper;

import malibu.requestmapper.annotation.ExceptionMapping;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class ExceptionHandlerMappingsTest {

    @ExceptionMapping(RuntimeException.class)
    public void testExceptonHanlder() {

    }

//    @ExceptionMapping(ConcreteRuntimeException.class)
//    public void testExceptonHanlder2() {
//
//    }

    @Test
    public void detectHandlerMethodsTest() throws NoSuchMethodException {
        final ExceptionHandlerMappings exceptionHandlerMappings = new ExceptionHandlerMappings(Collections.singletonList(this));
        exceptionHandlerMappings.detectHandlerMethods();

        final HandlerMethod handlerMethod = exceptionHandlerMappings.findHandler(RuntimeException.class); //등록된 Exception에 대한 hanlder 찾기
        assertNotNull(handlerMethod);

        final Method method = getClass().getMethod("testExceptonHanlder");
        assertEquals(method, handlerMethod.getMethod());
    }

    @Test
    public void detectHandlerMethodsTest2() throws NoSuchMethodException {
        final ExceptionHandlerMappings exceptionHandlerMappings = new ExceptionHandlerMappings(Collections.singletonList(this));
        exceptionHandlerMappings.detectHandlerMethods();

        final HandlerMethod handlerMethod = exceptionHandlerMappings.findHandler(ConcreteRuntimeException.class); //등록된 Exception를 extends 하는 자식 exception에 대한 handler 찾기
        assertNotNull(handlerMethod);

        final Method method = getClass().getMethod("testExceptonHanlder");
        assertEquals(method, handlerMethod.getMethod());
    }

    @Test
    public void detectHandlerMethodsTest3() {
        final ExceptionHandlerMappings exceptionHandlerMappings = new ExceptionHandlerMappings(Collections.singletonList(this));
        exceptionHandlerMappings.detectHandlerMethods();

        final HandlerMethod handlerMethod = exceptionHandlerMappings.findHandler(Exception.class); //등록되지 않은 Exception에 대한 hanlder 찾기
        assertNull(handlerMethod);
    }

    @Test
    public void detectHandlerMethodsTest4() throws NoSuchMethodException {
        final ExceptionHandlerMappings exceptionHandlerMappings = new ExceptionHandlerMappings(Collections.singletonList(this));
        exceptionHandlerMappings.detectHandlerMethods();

        final HandlerMethod handlerMethod = exceptionHandlerMappings.findHandler(GrandGrandChildRuntimeException.class); //등록된 Exception를 여러단계 상속하는 exception에 대한 handler 찾기
        assertNotNull(handlerMethod);

        final Method method = getClass().getMethod("testExceptonHanlder");
        assertEquals(method, handlerMethod.getMethod());
    }

    /**
     *
     */
    class ConcreteRuntimeException extends RuntimeException {

    }

    class GrandChildRuntimeException extends ConcreteRuntimeException {

    }

    class GrandGrandChildRuntimeException extends GrandChildRuntimeException {

    }
}
