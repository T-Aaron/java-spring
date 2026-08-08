package com.aaron.usermanagement.exception;

import com.aaron.usermanagement.dto.ApiResponse;
import jakarta.validation.ConstraintViolation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 1. Bắt các lỗi AppException tự định nghĩa (Sử dụng Enum ErrorCode trực tiếp)
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<Void>> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
    }

    // 2. Bắt lỗi Validation (Dữ liệu đầu vào)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handlingValidation(MethodArgumentNotValidException exception){
        var fieldError = exception.getFieldError();
        String enumKey = fieldError.getDefaultMessage();

        ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;
        Map<String, Object> attributes = null;

        try {
            errorCode = ErrorCode.valueOf(enumKey);
            // Trích xuất các thuộc tính từ Annotation (ví dụ: min, max...)
            var constraintViolation = fieldError.unwrap(ConstraintViolation.class);
            attributes = constraintViolation.getConstraintDescriptor().getAttributes();
        } catch (IllegalArgumentException e) {
            // Log lỗi nếu cần
        }

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(Objects.nonNull(attributes)
                        ? mapAttribute(errorCode.getMessage(), attributes)
                        : errorCode.getMessage())
                .build();

        return ResponseEntity.badRequest().body(apiResponse);
    }

    // Hàm Helper để thay thế placeholder {min} bằng giá trị thực tế
    private String mapAttribute(String message, Map<String, Object> attributes){
        Object minValue = attributes.get("value");// Lấy giá trị 'min' từ Annotation
        if (Objects.isNull(minValue)) {
            minValue = attributes.get("min");
        }
        return message.replace("{min}", String.valueOf(minValue));
    }

    // 3. Bắt lỗi phân quyền truy cập (403 Forbidden) ở tầng Method Security
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handlingAccessDeniedException(AccessDeniedException exception){
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ApiResponse.<Void>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }


    // 4. Bắt lỗi hệ thống chưa được định nghĩa trước
    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<ApiResponse<Void>> handlingGeneralException(RuntimeException exception){
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
                .message(exception.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
    }

}

