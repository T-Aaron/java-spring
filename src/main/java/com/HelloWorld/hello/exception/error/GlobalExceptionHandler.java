package com.HelloWorld.hello.exception.error;

import com.HelloWorld.hello.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // Helper để tạo Response chung
    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, List<ApiError> errors) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), errors));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex){
        List<ApiError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ApiError(e.getField(), e.getDefaultMessage()))
                .toList();
        return buildErrorResponse(HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex){
        // Sử dụng Helper đã viết ở trên
        ApiError error = new ApiError("USER_NOT_FOUND", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, List.of(error));
    }

    @ExceptionHandler(value = org.springframework.security.access.AccessDeniedException.class)
    ResponseEntity<ErrorResponse> handlingAccessDeniedException(AccessDeniedException exception){
        ApiError error = new ApiError("FORBIDEN", "Bạn không có quyền thực hiện hành động này!");
        return buildErrorResponse(HttpStatus.FORBIDDEN, List.of(error));
    }
}

