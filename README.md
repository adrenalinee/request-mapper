# Request mapper

요청을 특정 클래스의 특정 메서드가 처리한후 정해진응답을 할 수 있게 해주는 기능을 추상화 시켰습니다.


## feature
* http 가 아니라도 spring webMVC 스타일의 annotation 기반 개발을 가능하게 해줌
* 다양한 확장포인트를 통해 커스터 마이징 가능
* 확장 포인트 인터페이스들 이름이 spring webMVC 와 유사하여 이해하기 쉬움
* [reactor](https://projectreactor.io) 기반의 non-blocking 스타일과 blocking 스타일 개발 모두 사용가능
* request mapper 를 구현한 코드를 자체 framework 로 활용할 수 있음


## Concept
spring webMVC는 다음과 같은 핵심 기능을 가지고 있습니다.

1. 입력받은 HttpRequest 객체를 특정 클래스의 특정 메서드가 처리하게 한다.
2. 이 메서드가 리턴한 값을 HttpResponse 객체로 변환시켜 전달하다.
3. 이과정에서 일어나는 모든 상황들을 관리한다.

데이터의 흐름을 그림으로 그려보면 아래와 같습니다.

```
HttpRequest --> handlerClass.mappingMethod() --> HttpResponse
```

httpRequest가 아니라도 동일한 흐름으로 처리하는 기능이 필요한 경우가 있습니다. 그렇다고 spring webMVC에서 이 부분만
때어내서 사용하는 것은 불가능합니다. 그래서 입력 객체와 응답 객체가 정의되지 않은체 거의 똑같은 역할을 할 수 있는 코드만
작성해서 Spring webMVC를 추상화시켜 보았습니다.

입력 객체(input context)는 I 라는 제네릭 타입으로 정의 하고 출력객체(output context)는 O 라는 이름의 제네릭 타입으로 정의하였습니다.

다음은 이를 처리하는 dispatcher 객체의 기본적인 코드입니다.

```java
/**
 * @param <I> - input
 * @param <O> - output
 */
public class RequestDispatcher<I, O> {

    public Optional<O> handle(I inputContext) throws Throwable {
        
    }
    
    
}
```
개발자는 request 객체와 response 객체를 정의하고 request mapper 에서 제공하는 각종 확장 포인트를 1차적으로 작성해서
spring webMVC 와 유사한 framework로 활용할 수 있습니다.


## getting started

1. input context, output context 정의
2. RequestDispater 초기화
3. handler 구현


## basic usage

### request handler methods


#### Method argument

#### Type conversion

#### Return value






## implements guide


### RequestHandlerResultHandler

### RequestMappingCondition

### RequestMappingConditionCreater

### RequestMethodArgumentResolver

### TypeConverter

### RequestHandlerInterceptor

### @RequestHandler

### @RequestMapping

### @RequestParam






## Roadmap
* jsr-303 기반의 validation 기능 추가 예정
* 효과적인 cache 적용 예정
* Functional endpoint 지원 예정





## Architecture

### core component

 * **RequestHandlerMappings**: request 가 어떤  handler method를 호출해야 할지정보를 모두 저장하고 있는 객체
 * **RequestHandlerMethodInvoker**: handler method를 호출하고 응답을 받아오는 객체
 * **RequestHandlerResultHandlers**: handler method가 리턴한 return value 를 최종 output object로 만들어주는 객체
 * **InterceptorRegistry**: request handler method 를 실행할때 전처리, 후처리를 해줄 interceptor를 모두 저장하고 있는 객체
 
 그외 확장을 위한 interface 는 아래에서 설명합니다.

### bootstrap
어플리케이션을 로딩할때 request mapper를 초기화 해야 합니다. RequestDispatcher.bootstrap()을 호출합니다. 아래와 같은 동작들을 합니다.
 * register RequestHandlerInterceptor
 * register TypeConvertor
 * register RequestMappingConditionCreater
 * register RequestMethodArgumentResolver
 * register RequestHandlerResultHandler
 * set etc configuration
 * detect request handler methods
 * detect exception handler methods
 

### request handle
RequestDispatcher.handler() 의 동작을 조금더 상세히 볼 수 있는 코드입니다.
```java
/**
 * @param <I> - input
 * @param <O> - output
 */
public class RequestDispatcher<I, O> {

    public Optional<O> handle(I inputContext) throws Throwable {
        HandlerMethod handlerMethod = handlerMappings.findHandler(inputContext);
        RequestHandlerResult handlerResult = handlerMethodInvoker.invoke(inputContext, handlerMethod);
        RequestHandlerResultHandler<O> resultHandler = handlerResultHandlers.findResultHandler(handlerResult);
        O output = resultHandler.handleResult(handlerResult);
        
        return Optional.ofNullable(output);
    }
    
    
}
```
한줄씩 설명
 * inputContext를 보고 handler method를 찾는다.
 * handler method를 실행(invoke)하고 handler result를 리턴받는다.
 * handler result를 처리할 handler result handler 를 찾는다.
 * handler result handler 에서 최종 output 객체를 리턴 받는다.
 * output을 Optional로 감싸서 리턴한다.

실제로는 위 코드에서 interceptor를 호출하고 예외 상황처리 코드가 추가 됩니다.
