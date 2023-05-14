package malibu.requestmapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.lang.reflect.Type;

@ToString
@Getter
@AllArgsConstructor
public class RequestHandlerResult {

    /**
     * method 가 실제로 리턴한 객체
     */
    private final Object returnValue;

    /**
     * method 스팩상 리턴 타입
     */
    private final Type returnType;

    private final HandlerMethod handlerMethod;
}
