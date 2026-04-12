package com.HelloWorld.hello.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "uncategorize error"),
    USER_EXISTED(1001, "User existed"),
    USERNAME_INVALID(1002, "User name invalid, at least 3 characters"),
    INVALID_PASSWORD(1003, "Password invalid, must at least 6 characters"),
    USER_NOT_FOUND(1004, "User not found"),
    UNAUTHORIZED(1005, "You do not have permission")
    ;

    ErrorCode(int code, String message){
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;
}
