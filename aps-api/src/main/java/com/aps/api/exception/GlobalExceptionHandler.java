package com.aps.api.exception;

import com.aps.api.dto.AjaxResult;
import com.aps.service.exception.BusinessException;
import com.aps.service.exception.ImportValidationException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<AjaxResult<Void>> handleBusinessException(BusinessException exception) {
        return ResponseEntity.status(exception.getCode())
                .body(AjaxResult.error(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(ImportValidationException.class)
    public ResponseEntity<AjaxResult<java.util.List<com.aps.service.MaterialService.MaterialImportFailure>>> handleImportValidationException(ImportValidationException exception) {
        return ResponseEntity.badRequest()
                .body(AjaxResult.error(400, exception.getMessage(), exception.getFailures()));
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AjaxResult<Void>> handleIllegalArgumentException(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(AjaxResult.error(400, "请求参数格式不正确"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AjaxResult<Void>> handleException(Exception exception) {
        log.error("Unhandled server exception", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AjaxResult.error(500, "系统内部错误"));
    }
}
