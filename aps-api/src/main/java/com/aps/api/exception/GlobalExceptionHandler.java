package com.aps.api.exception;

import com.aps.api.dto.AjaxResult;
import com.aps.service.exception.BusinessException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<AjaxResult<Void>> handleBusinessException(BusinessException exception) {
        return ResponseEntity.status(exception.getCode())
                .body(AjaxResult.error(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AjaxResult<Map<String, String>>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(AjaxResult.error(400, "参数校验失败", errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<AjaxResult<Void>> handleConstraintViolationException(ConstraintViolationException exception) {
        return ResponseEntity.badRequest().body(AjaxResult.error(400, exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AjaxResult<Void>> handleException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AjaxResult.error(500, exception.getMessage() == null ? "系统内部错误" : exception.getMessage()));
    }
}
