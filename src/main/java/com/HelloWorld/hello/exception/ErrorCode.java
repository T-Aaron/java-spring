package com.HelloWorld.hello.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "uncategorize error"),
    USER_EXISTED(1001, "User existed"),
    USER_NOT_FOUND(1004, "User not found"),
    UNAUTHORIZED(1005, "You do not have permission"),

    USERNAME_INVALID(1002, "User name invalid, at least {min} characters"),
    INVALID_PASSWORD(1003, "Password invalid, must at least {min} characters"),
    AGE_INVALID(1005, "You must be at least {min} years old"),
    ;

    ErrorCode(int code, String message){
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;
}
