package malibu.requestmapper;


import malibu.requestmapper.annotation.ExceptionMapping;
import malibu.requestmapper.exception.AlreayBootstrapedException;
import malibu.requestmapper.exception.ExceptionRequestException;
import malibu.requestmapper.exception.RequestExecutionException;
import malibu.requestmapper.impl.TestRequest;
import malibu.requestmapper.impl.TestResponse;
import malibu.requestmapper.implement.ByPassRequestMapperConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServletRequestDispatcherTest {

    public TestResponse narmalNoArguHandler() {
        return new TestResponse();
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

    @ExceptionMapping(Exception.class)
    public TestResponse exceptionHandler(Exception e) {
        return new TestResponse().setException(e);
    }

    @ExceptionMapping(TestException.class)
    public TestResponse testExceptionHandler(TestException e) {
        return new TestResponse().setException(e);
    }


    @Test
    @DisplayName("bootstrap()을 한번 이상 호출하면 에러나는지 확인")
    public void notAllowOneMoreBootstrapTest() {
        final OptionalRequestDispatcher<TestRequest, TestResponse> requestDispatcher = new OptionalRequestDispatcher<>();
        requestDispatcher.addRequestMapperConfigurer(
                new ByPassRequestMapperConfiguration<>(
                        TestRequest.class,
                        TestResponse.class
                )
        );
        requestDispatcher.bootstrap();

        assertThrows(AlreayBootstrapedException.class, () -> requestDispatcher.bootstrap());
    }

    @Test
    @DisplayName("handler 호출 정상동작하는지 확인")
    public void normalHandleTest() throws NoSuchMethodException {
        final Method requestMethod = getClass().getMethod("narmalNoArguHandler");

        final OptionalRequestDispatcher<TestRequest, TestResponse> requestDispatcher = new OptionalRequestDispatcher<>();
        requestDispatcher.addRequestMapperConfigurer(
                new ByPassRequestMapperConfiguration<>(
                        TestRequest.class,
                        TestResponse.class
                )
        );
        requestDispatcher.registerHandlerObject(this);
        requestDispatcher.registerMappingConditionCreator(new RequestMappingConditionCreator<TestRequest>() {
            @Override
            public boolean isRequire(HandlerMethod handlerMethod) {
                return true;
            }

            @Override
            public RequestMappingCondition<TestRequest> createCondition(HandlerMethod handlerMethod) {
                return inputContext -> true;
            }
        });
        requestDispatcher.registerHandlerResultHandler(new RequestHandlerResultHandler<TestResponse>() {
            @Override
            public boolean isSupports(RequestHandlerResult handlerResult) {
                return true;
            }

            @Override
            public TestResponse handleResult(RequestHandlerResult handlerResult) {
                return (TestResponse) handlerResult.getReturnValue();
            }
        });
        requestDispatcher.addRequestMapperConfigurer(new RequestMapperConfigurer<TestRequest, TestResponse>() {
            @Override
            public void configureHandlerMappings(RequestHandlerMappings<TestRequest> handlerMappings) {
                handlerMappings.registerHandlerMethod(ServletRequestDispatcherTest.this, requestMethod);
            }
        });

//        requestDispatcher.configureHandlerMappings(mappings -> {
//            mappings.addMappingConditionCreator(new RequestMappingConditionCreator<TestRequest>() {
//                @Override
//                public boolean isRequire(HandlerMethod handlerMethod) {
//                    return true;
//                }
//
//                @Override
//                public RequestMappingCondition<TestRequest> createCondition(HandlerMethod handlerMethod) {
//                    return inputContext -> true;
//                }
//            });
//
//            mappings.registerHandlerMethod(this, requestMethod);
//        });
//        requestDispatcher.configureHandlerResultHandlers(resultHandlers -> {
//            resultHandlers.addHandlerResultHandler(new RequestHandlerResultHandler<TestResponse>() {
//                @Override
//                public boolean isSupports(RequestHandlerResult handlerResult) {
//                    return true;
//                }
//
//                @Override
//                public TestResponse handleResult(RequestHandlerResult handlerResult) {
//                    return (TestResponse) handlerResult.getReturnValue();
//                }
//            });
//        });

        requestDispatcher.bootstrap();
        final Optional<TestResponse> resOpt = requestDispatcher.handle(new TestRequest());
        assertNotNull(resOpt);
        assertTrue(resOpt.isPresent());
    }

    @Test
    @DisplayName("handler에서 exception 발생하고 exception handler 등록이 안되어 있을 경우 HandlerExecutionException이 throw 되는지 확인")
    public void invokeWithThrowErrorTest() throws NoSuchMethodException {
        final Method requestMethod = getClass().getMethod("testRequestHandler");

        final OptionalRequestDispatcher<TestRequest, TestResponse> requestDispatcher = new OptionalRequestDispatcher<>();
        requestDispatcher.addRequestMapperConfigurer(
                new ByPassRequestMapperConfiguration<>(
                        TestRequest.class,
                        TestResponse.class
                )
        );
        requestDispatcher.registerMappingConditionCreator(new RequestMappingConditionCreator<TestRequest>() {
            @Override
            public boolean isRequire(HandlerMethod handlerMethod) {
                return true;
            }

            @Override
            public RequestMappingCondition<TestRequest> createCondition(HandlerMethod handlerMethod) {
                return inputContext -> true;
            }
        });
        requestDispatcher.registerHandlerResultHandler(new RequestHandlerResultHandler<TestResponse>() {
            @Override
            public boolean isSupports(RequestHandlerResult handlerResult) {
                return true;
            }

            @Override
            public TestResponse handleResult(RequestHandlerResult handlerResult) {
                return (TestResponse) handlerResult.getReturnValue();
            }
        });
        requestDispatcher.addRequestMapperConfigurer(new RequestMapperConfigurer<TestRequest, TestResponse>() {
            @Override
            public void configureHandlerMappings(RequestHandlerMappings<TestRequest> handlerMappings) {
                handlerMappings.registerHandlerMethod(ServletRequestDispatcherTest.this, requestMethod);
            }
        });

//        requestDispatcher.configureHandlerMappings(mappings -> {
//            mappings.addMappingConditionCreator(new RequestMappingConditionCreator<TestRequest>() {
//                @Override
//                public boolean isRequire(HandlerMethod handlerMethod) {
//                    return true;
//                }
//
//                @Override
//                public RequestMappingCondition<TestRequest> createCondition(HandlerMethod handlerMethod) {
//                    return inputContext -> true;
//                }
//            });
//
//            mappings.registerHandlerMethod(this, requestMethod);
//        });
//        requestDispatcher.configureHandlerResultHandlers(resultHandlers -> {
//            resultHandlers.addHandlerResultHandler(new RequestHandlerResultHandler<TestResponse>() {
//                @Override
//                public boolean isSupports(RequestHandlerResult handlerResult) {
//                    return true;
//                }
//
//                @Override
//                public TestResponse handleResult(RequestHandlerResult handlerResult) {
//                    return (TestResponse) handlerResult.getReturnValue();
//                }
//            });
//        });

        requestDispatcher.bootstrap();

        assertThrows(RequestExecutionException.class, () -> requestDispatcher.handle(new TestRequest()));
    }


    @Test
    @DisplayName("handler에서 exception 발생하고 exception handler가 등록되어 있을 경우 exception handler가 잘 실행되는지 확인")
    public void exceptionHandlerTest() throws Throwable {
        final Method requestMethod = getClass().getMethod("testRequestHandler");

        final OptionalRequestDispatcher<TestRequest, TestResponse> requestDispatcher = new OptionalRequestDispatcher<>();
        requestDispatcher.addRequestMapperConfigurer(
                new ByPassRequestMapperConfiguration<>(
                        TestRequest.class,
                        TestResponse.class
                )
        );
        requestDispatcher.registerHandlerObject(this);
        requestDispatcher.registerMappingConditionCreator(new RequestMappingConditionCreator<TestRequest>() {
                @Override
                public boolean isRequire(HandlerMethod handlerMethod) {
                    return true;
                }

                @Override
                public RequestMappingCondition<TestRequest> createCondition(HandlerMethod handlerMethod) {
                    return inputContext -> true;
                }
            });
        requestDispatcher.registerHandlerResultHandler(new RequestHandlerResultHandler<TestResponse>() {
                @Override
                public boolean isSupports(RequestHandlerResult handlerResult) {
                    return true;
                }

                @Override
                public TestResponse handleResult(RequestHandlerResult handlerResult) {
                    return (TestResponse) handlerResult.getReturnValue();
                }
            });
        requestDispatcher.addRequestMapperConfigurer(new RequestMapperConfigurer<TestRequest, TestResponse>() {
            @Override
            public void configureHandlerMappings(RequestHandlerMappings<TestRequest> handlerMappings) {
                handlerMappings.registerHandlerMethod(ServletRequestDispatcherTest.this, requestMethod);
            }
        });

//        requestDispatcher.configureHandlerMappings(mappings -> {
//            mappings.addMappingConditionCreator(new RequestMappingConditionCreator<TestRequest>() {
//                @Override
//                public boolean isRequire(HandlerMethod handlerMethod) {
//                    return true;
//                }
//
//                @Override
//                public RequestMappingCondition<TestRequest> createCondition(HandlerMethod handlerMethod) {
//                    return inputContext -> true;
//                }
//            });
//
//            mappings.registerHandlerMethod(this, requestMethod);
//        });
//        requestDispatcher.configureHandlerResultHandlers(resultHandlers -> {
//            resultHandlers.addHandlerResultHandler(new RequestHandlerResultHandler<TestResponse>() {
//                @Override
//                public boolean isSupports(RequestHandlerResult handlerResult) {
//                    return true;
//                }
//
//                @Override
//                public TestResponse handleResult(RequestHandlerResult handlerResult) {
//                    return (TestResponse) handlerResult.getReturnValue();
//                }
//            });
//        });

        requestDispatcher.bootstrap();
        final Optional<TestResponse> resOpt = requestDispatcher.handle(new TestRequest());
        assertNotNull(resOpt);
        assertTrue(resOpt.isPresent());
        assertEquals(TestException.class, resOpt.get().getException().getClass());
    }


    @Test
    @DisplayName("exception handler에서 exception 발생시 상위(spuer) exception 핸들러가 처리하는지 확인")
    public void invokeWithThrowErrorTest2() throws Throwable {
        final Method requestMethod = getClass().getMethod("testNullpointerRequestHandler");

        final OptionalRequestDispatcher<TestRequest, TestResponse> requestDispatcher = new OptionalRequestDispatcher<>();
        requestDispatcher.addRequestMapperConfigurer(
                new ByPassRequestMapperConfiguration<>(
                        TestRequest.class,
                        TestResponse.class
                )
        );
        requestDispatcher.registerHandlerObject(this);
        requestDispatcher.registerMappingConditionCreator(new RequestMappingConditionCreator<TestRequest>() {
                @Override
                public boolean isRequire(HandlerMethod handlerMethod) {
                    return true;
                }

                @Override
                public RequestMappingCondition<TestRequest> createCondition(HandlerMethod handlerMethod) {
                    return inputContext -> true;
                }
            });
        requestDispatcher.registerHandlerResultHandler(new RequestHandlerResultHandler<TestResponse>() {
                @Override
                public boolean isSupports(RequestHandlerResult handlerResult) {
                    return true;
                }

                @Override
                public TestResponse handleResult(RequestHandlerResult handlerResult) {
                    return (TestResponse) handlerResult.getReturnValue();
                }
            });
        requestDispatcher.addRequestMapperConfigurer(new RequestMapperConfigurer<TestRequest, TestResponse>() {
            @Override
            public void configureHandlerMappings(RequestHandlerMappings<TestRequest> handlerMappings) {
                handlerMappings.registerHandlerMethod(ServletRequestDispatcherTest.this, requestMethod);
            }
        });

//        requestDispatcher.configureHandlerMappings(mappings -> {
//            mappings.addMappingConditionCreator(new RequestMappingConditionCreator<TestRequest>() {
//                @Override
//                public boolean isRequire(HandlerMethod handlerMethod) {
//                    return true;
//                }
//
//                @Override
//                public RequestMappingCondition<TestRequest> createCondition(HandlerMethod handlerMethod) {
//                    return inputContext -> true;
//                }
//            });
//
//            mappings.registerHandlerMethod(this, requestMethod);
//        });
//        requestDispatcher.configureHandlerResultHandlers(resultHandlers -> {
//            resultHandlers.addHandlerResultHandler(new RequestHandlerResultHandler<TestResponse>() {
//                @Override
//                public boolean isSupports(RequestHandlerResult handlerResult) {
//                    return true;
//                }
//
//                @Override
//                public TestResponse handleResult(RequestHandlerResult handlerResult) {
//                    return (TestResponse) handlerResult.getReturnValue();
//                }
//            });
//        });

        requestDispatcher.bootstrap();
        final Optional<TestResponse> resOpt = requestDispatcher.handle(new TestRequest());
        assertNotNull(resOpt);
        assertTrue(resOpt.isPresent());
        assertNotNull(resOpt.get().getException());
        assertEquals(resOpt.get().getException().getClass(), NullPointerException.class);
    }

    @Test
    @DisplayName("handler에서 exception 발생하고 exception handler 등록이 안되어 있을 경우 HandlerExecutionException이 throw 되는지 확인")
    public void invokeWithExceptionHadlerThrowErrorTest() throws NoSuchMethodException {
        final Method requestMethod = getClass().getMethod("testRequestHandler");

        final OptionalRequestDispatcher<TestRequest, TestResponse> requestDispatcher = new OptionalRequestDispatcher<>();
        requestDispatcher.addRequestMapperConfigurer(
                new ByPassRequestMapperConfiguration<>(
                        TestRequest.class,
                        TestResponse.class
                )
        );
        requestDispatcher.registerHandlerObject(new ExceptionHandlerThrow());
        requestDispatcher.registerMappingConditionCreator(new RequestMappingConditionCreator<TestRequest>() {
            @Override
            public boolean isRequire(HandlerMethod handlerMethod) {
                return true;
            }

            @Override
            public RequestMappingCondition<TestRequest> createCondition(HandlerMethod handlerMethod) {
                return inputContext -> true;
            }
        });
        requestDispatcher.registerHandlerResultHandler(new RequestHandlerResultHandler<TestResponse>() {
            @Override
            public boolean isSupports(RequestHandlerResult handlerResult) {
                return true;
            }

            @Override
            public TestResponse handleResult(RequestHandlerResult handlerResult) {
                return (TestResponse) handlerResult.getReturnValue();
            }
        });
        requestDispatcher.addRequestMapperConfigurer(new RequestMapperConfigurer<TestRequest, TestResponse>() {
            @Override
            public void configureHandlerMappings(RequestHandlerMappings<TestRequest> handlerMappings) {
                handlerMappings.registerHandlerMethod(ServletRequestDispatcherTest.this, requestMethod);
            }
        });

//        requestDispatcher.configureHandlerMappings(mappings -> {
//            mappings.addMappingConditionCreator(new RequestMappingConditionCreator<TestRequest>() {
//                @Override
//                public boolean isRequire(HandlerMethod handlerMethod) {
//                    return true;
//                }
//
//                @Override
//                public RequestMappingCondition<TestRequest> createCondition(HandlerMethod handlerMethod) {
//                    return inputContext -> true;
//                }
//            });
//
//            mappings.registerHandlerMethod(this, requestMethod);
//        });
//        requestDispatcher.configureHandlerResultHandlers(resultHandlers -> {
//            resultHandlers.addHandlerResultHandler(new RequestHandlerResultHandler<TestResponse>() {
//                @Override
//                public boolean isSupports(RequestHandlerResult handlerResult) {
//                    return true;
//                }
//
//                @Override
//                public TestResponse handleResult(RequestHandlerResult handlerResult) {
//                    return (TestResponse) handlerResult.getReturnValue();
//                }
//            });
//        });

        requestDispatcher.bootstrap();

        assertThrows(ExceptionRequestException.class, () -> requestDispatcher.handle(new TestRequest()));
    }

    static class ExceptionHandlerThrow {

    @ExceptionMapping(Exception.class)
    public void testExceptionHandler() throws Exception {
        throw new Exception();
    }
    }
}
