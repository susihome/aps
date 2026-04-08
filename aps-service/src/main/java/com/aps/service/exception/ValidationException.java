package com.aps.service.exception;

public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super(400, message);
    }
}
