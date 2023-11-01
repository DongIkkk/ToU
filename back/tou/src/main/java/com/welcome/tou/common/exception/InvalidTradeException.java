package com.welcome.tou.common.exception;

public class InvalidTradeException extends RuntimeException{

    public static final String CANT_SAME_BRANCH = "동일한 지점의 거래는 불가능합니다.";

    public InvalidTradeException(String message) {
        super(message);
    }
}
