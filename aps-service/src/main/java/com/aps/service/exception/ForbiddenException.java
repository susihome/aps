package com.aps.service.exception;

public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super(403, message);
    }
}
