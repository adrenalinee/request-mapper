package malibu.requestmapper.exception;

public class RequestMapperConfigurerNotFoundException
    extends RequestMapperException {

    public RequestMapperConfigurerNotFoundException() {
        super("RequestMapperConfigurer 가 최소 하나라도 등록되어 있어야 handler 가 동작할 수 있습니다.");
    }
}
