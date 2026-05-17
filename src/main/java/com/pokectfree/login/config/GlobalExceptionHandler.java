package com.pokectfree.login.config;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 서비스 레이어에서 던진 비즈니스 예외를 적절한 HTTP 상태 코드로 변환합니다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * "워크스페이스를 찾을 수 없습니다" 등 리소스 미존재 예외 → 404
     * "접근 권한이 없습니다" 등 권한 부족 예외 → 403
     * 그 외 비즈니스 입력 오류 → 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "잘못된 요청입니다.";

        if (message.contains("찾을 수 없습니다")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", message));
        }
        if (message.contains("접근 권한") || message.contains("소유자만")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", message));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", message));
    }
}
