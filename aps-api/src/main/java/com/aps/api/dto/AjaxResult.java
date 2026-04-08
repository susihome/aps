package com.aps.api.dto;

public record AjaxResult<T>(int code, String message, T data) {

    public static <T> AjaxResult<T> success(T data) {
        return new AjaxResult<>(200, "success", data);
    }

    public static AjaxResult<Void> success() {
        return new AjaxResult<>(200, "success", null);
    }

    public static AjaxResult<Void> error(int code, String message) {
        return new AjaxResult<>(code, message, null);
    }

    public static <T> AjaxResult<T> error(int code, String message, T data) {
        return new AjaxResult<>(code, message, data);
    }
}
