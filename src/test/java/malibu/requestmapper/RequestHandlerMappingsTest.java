package malibu.requestmapper;

import malibu.requestmapper.annotation.RequestMapping;
import malibu.requestmapper.exception.MatchedRequestNotFoundException;
import malibu.requestmapper.impl.TestRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

public class RequestHandlerMappingsTest {

    @RequestMapping
    public void testHanlder() {

    }

    public void notMatchedHandler() {

    }

    @Test
    @DisplayName("아무설정없이 handler를 못 찾으면 기본 notMatchedHandlerMethod 를 리턴 하는지 확인")
    public void findHandlerTestWithDefaultNotMatchedHandlerMethod() throws NoSuchMethodException {
        final RequestHandlerMappings<TestRequest> requestRequestHandlerMappings = new RequestHandlerMappings<>(singletonList(this));
        requestRequestHandlerMappings.setNotMatchedHandlerMethod(
            new HandlerMethod(this, getClass().getMethod("notMatchedMapping"))
        );

        final TestRequest testRequest = new TestRequest();
        final HandlerMethod returnedHandlerMethod = requestRequestHandlerMappings.findHandler(testRequest);
        assertEquals(requestRequestHandlerMappings.getNotMatchedHandlerMethod(), returnedHandlerMethod);
    }

    @Test
    @DisplayName("notMatchedHandlerMethod를 null 로 셋팅하고 handler를 못 찾으면 에러 발생하는지 확인")
    public void findHandlerTestWithThrowHandlerNotFoundException() {
        final RequestHandlerMappings<TestRequest> requestRequestHandlerMappings = new RequestHandlerMappings<>(singletonList(this));
        requestRequestHandlerMappings.setNotMatchedHandlerMethod(null);

        final TestRequest testRequest = new TestRequest();
        assertThrows(MatchedRequestNotFoundException.class, () -> requestRequestHandlerMappings.findHandler(testRequest));
    }

    @Test
    @DisplayName("notMatchedHandlerMethod 를 등록하고 handler를 못 찾으면 등록한 notMatchedHandlerMethod를 리턴하는지 확인")
    public void findHandlerTestWithNotMatchedHandlerMethod() throws NoSuchMethodException {
        final Method method = getClass().getMethod("notMatchedHandler");
        final HandlerMethod notMatchedHandlerMethod = new HandlerMethod(this, method);

        final RequestHandlerMappings<TestRequest> requestRequestHandlerMappings = new RequestHandlerMappings<>(singletonList(this));
        requestRequestHandlerMappings.setNotMatchedHandlerMethod(notMatchedHandlerMethod);

        final TestRequest testRequest = new TestRequest();
        final HandlerMethod returnedHandlerMethod = requestRequestHandlerMappings.findHandler(testRequest);
        assertEquals(notMatchedHandlerMethod, returnedHandlerMethod);
    }

    @Test
    @DisplayName("handler 잘 찾는지 확인")
    public void findHandlerTest() throws NoSuchMethodException {
        final RequestHandlerMappings<TestRequest> requestRequestHandlerMappings = new RequestHandlerMappings<>(singletonList(this));
        requestRequestHandlerMappings.addMappingConditionCreator(new TestRequestMappingConditionCreator());
        requestRequestHandlerMappings.detectRequestHandlerMethods();

        final TestRequest testRequest = new TestRequest();
        final HandlerMethod returnedHandlerMethod = requestRequestHandlerMappings.findHandler(testRequest);
        assertNotEquals(requestRequestHandlerMappings.getNotMatchedHandlerMethod(), returnedHandlerMethod);

        final Method testHanlderMethod = getClass().getMethod("testHanlder");
        assertEquals(testHanlderMethod, returnedHandlerMethod.getMethod());
    }

    public void notMatchedMapping() {

    }
}

class TestRequestMappingConditionCreator implements RequestMappingConditionCreator<TestRequest> {

    @Override
    public boolean isRequire(HandlerMethod handlerMethod) {
        return true;
    }

    @Override
    public RequestMappingCondition<TestRequest> createCondition(HandlerMethod handlerMethod) {
        return inputContext -> handlerMethod.getMethod().getName().equals("testHanlder");
    }
}
