package com.aps.service.exception;

public class TokenInvalidException extends BusinessException {

    public TokenInvalidException(String message) {
        super(401, message);
    }
}
