package com.example.aprbackendassignment.common;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final String code;    // "200", "400", "409" 등 문자열 코드
    private final String message; // "OK", "Bad Request" 등
    private final T data;

    private ApiResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>("200", "OK", data);
    }

    public static <T> ApiResponse<T> of(String code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }

    public static ApiResponse<Void> message(String code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public T getData() { return data; }

}
