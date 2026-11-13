package com.hackathon.global.error;

import com.hackathon.global.response.ApiResponse;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        return toResponse(e.getErrorCode(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        f -> f.getField(),
                        f -> f.getDefaultMessage() == null ? "" : f.getDefaultMessage(),
                        (a, b) -> a));
        return toResponse(ErrorCode.INVALID_INPUT, errors);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception e) {
        return toResponse(ErrorCode.INVALID_INPUT, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException e) {
        return toResponse(ErrorCode.NOT_FOUND, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception e) {
        // 405, 415, 필수 헤더/파라미터 누락 같은 스프링 표준 예외는 500이 아니라 제 상태코드로 내려준다
        if (e instanceof ErrorResponse standard) {
            int status = standard.getStatusCode().value();
            return ResponseEntity.status(status)
                    .body(ApiResponse.fail("COMMON" + status, e.getMessage(), null));
        }
        log.error("Unhandled exception", e);
        return toResponse(ErrorCode.INTERNAL_ERROR, null);
    }

    private <T> ResponseEntity<ApiResponse<T>> toResponse(ErrorCode code, T data) {
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.fail(code.getCode(), code.getMessage(), data));
    }
}
