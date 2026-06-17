package com.HelloWorld.hello.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "uncategorize error"),
    USER_EXISTED(1001, "User existed"),
    USER_NOT_FOUND(1004, "User not found"),

    // 🔐 Bộ đôi lỗi Bảo mật chuẩn chỉnh:
    UNAUTHENTICATED(1005, "Unauthenticated (Token invalid or expired)"),
    UNAUTHORIZED(1006, "You do not have permission"),
    TOKEN_INVALIDATED(1008, "Token has been invalidated"), // 🌟 Bổ sung mã lỗi riêng biệt

    // 📝 Bộ lỗi Validation dữ liệu đầu vào:
    USERNAME_INVALID(1002, "User name invalid, at least {min} characters"),
    INVALID_PASSWORD(1003, "Password invalid, must at least {min} characters"),
    AGE_INVALID(1007, "You must be at least {min} years old"),
    ;

    ErrorCode(int code, String message){
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;
}
