package com.aps.service.exception;

public class InvalidCredentialsException extends BusinessException {

    public InvalidCredentialsException(String message) {
        super(401, message);
    }
}
