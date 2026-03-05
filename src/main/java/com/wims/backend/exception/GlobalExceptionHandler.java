package com.wims.backend.exception;

import com.wims.backend.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.security.access.AccessDeniedException;

@ControllerAdvice // Đánh dấu đây là nơi bắt lỗi toàn cục
public class GlobalExceptionHandler {

    // 1. Bắt lỗi tự tạo
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<?>> handlingAppException(AppException exception) {
        ApiResponse<?> apiResponse = ApiResponse.error(exception.getErrorCode(), exception.getMessage()).build();
        
        // Nếu là lỗi liên quan đến Auth (Token hết hạn, User không tồn tại...) -> Trả về 401
        int errorCode = exception.getErrorCode();
        if (errorCode == 1005 || errorCode == 1006 || errorCode == 986) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
        }

        return ResponseEntity.badRequest().body(apiResponse);
    }

    // 2. Bắt các lỗi còn lại chưa lường trước (RuntimeException)
    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<ApiResponse<?>> handlingRuntimeException(RuntimeException exception) {
        ApiResponse<?> apiResponse = ApiResponse.error(9999, exception.getMessage()).build();
        return ResponseEntity.badRequest().body(apiResponse);
    }

    // 3. Bắt lỗi Valid
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<?>> handlingValidation(MethodArgumentNotValidException exception) {
        // Lấy lỗi đầu tiên tìm thấy (ví dụ: "Giá tiền không được âm")
        String message = exception.getFieldError().getDefaultMessage();

        ApiResponse<?> apiResponse = ApiResponse.error(1002, message).build();
        return ResponseEntity.badRequest().body(apiResponse);
    }

    // 4. Thêm handler bắt lỗi 403 Forbidden
    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse<?>> handlingAccessDeniedException(AccessDeniedException exception) {
        ApiResponse<?> apiResponse = ApiResponse
                .error(987, "Bạn không có quyền thực hiện chức năng này (Chỉ dành cho Admin)").build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResponse);
    }
}