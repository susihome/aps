package com.aps.service.exception;

/**
 * 资源冲突异常 - 对应 HTTP 409
 */
public class ResourceConflictException extends BusinessException {

    public ResourceConflictException(String message) {
        super(409, message);
    }
}
