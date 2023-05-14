package malibu.requestmapper.exception;

public class AlreayBootstrapedException extends RequestMapperException {

    public AlreayBootstrapedException() {
        super("이미 초기화 되었습니다. 초기화는 한번만 가능합니다.");
    }
}
