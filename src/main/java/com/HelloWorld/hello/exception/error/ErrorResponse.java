package com.HelloWorld.hello.exception.error;

import java.util.List;

public class ErrorResponse {
    private int status;
    private List<ApiError> errors;

    public ErrorResponse(int status, List<ApiError> errors) {
        this.status = status;
        this.errors = errors;
    }

    public int getStatus() {
        return status;
    }

    public List<ApiError> getErrors() {
        return errors;
    }
}
