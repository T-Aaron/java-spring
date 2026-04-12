package com.HelloWorld.hello.exception.error;

import com.HelloWorld.hello.dto.response.ApiResponse;
import com.HelloWorld.hello.exception.ErrorCode;
import com.HelloWorld.hello.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // Helper để tạo Response chung
//    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, List<ApiError> errors) {
//        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), errors));
//    }

//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex){
//        List<ApiError> errors = ex.getBindingResult().getFieldErrors().stream()
//                .map(e -> new ApiError(e.getField(), e.getDefaultMessage()))
//                .toList();
//        return buildErrorResponse(HttpStatus.BAD_REQUEST, errors);
//    }

    // 1. Bắt lỗi Validation (Dữ liệu đầu vào)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handlingValidation(MethodArgumentNotValidException exception){
        String enumKey = exception.getFieldError().getDefaultMessage();
//        ErrorCode errorCode = ErrorCode.valueOf(enumKey);

        // Mẹo: Đề phòng trường hợp message trong Request không khớp với tên Enum
        ErrorCode errorCode = ErrorCode.USERNAME_INVALID;
        try {
            errorCode = ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException e) {
            // Log lỗi nếu cần
        }

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }

    // 2. Bắt lỗi không tìm thấy User (Thay thế UserNotFoundException cũ)
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse> handleUserNotFound(UserNotFoundException exception){
        // Sử dụng Helper đã viết ở trên
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(ErrorCode.USER_NOT_FOUND.getCode());
        apiResponse.setMessage(ErrorCode.USER_NOT_FOUND.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
    }

    // 3. Bắt lỗi phân quyền
    @ExceptionHandler(value = org.springframework.security.access.AccessDeniedException.class)
    ResponseEntity<ApiResponse> handlingAccessDenied(AccessDeniedException exception){
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(ErrorCode.UNAUTHORIZED.getCode());
        apiResponse.setMessage(ErrorCode.UNAUTHORIZED.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResponse);
    }

    // 4. Bắt tất cả các lỗi Runtime còn lại
    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<ApiResponse<Objects>> handlingRuntimeExeption(RuntimeException exception){
        ApiResponse<Objects> apiResponse = new ApiResponse<>();
        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());// Mã lỗi hệ thống chung
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }
}

