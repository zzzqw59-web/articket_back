package com.project.articket.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<Map<String, String>> handleFileUploadException(
            FileUploadException e) {
        log.error("파일 업로드 처리 중 오류가 발생했습니다.", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "FILE_UPLOAD_ERROR",
                        "message", e.getMessage()
                ));
    }

    // Controller와 Service에서 명시한 HTTP 상태코드를 변경하지 않는다.
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(
            ResponseStatusException e) {
        return ResponseEntity
                .status(e.getStatusCode())
                .body(Map.of(
                        "error", "HTTP_" + e.getStatusCode().value(),
                        "message", e.getReason() == null
                                ? "요청을 처리할 수 없습니다."
                                : e.getReason()
                ));
    }

    // Controller 내부에서 발생한 접근 제한만 처리한다.
    // Security Filter에서 직접 반환한 403 응답은 변경하지 않는다.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(
            AccessDeniedException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "error", "ACCESS_DENIED",
                        "message", "접근 권한이 없습니다."
                ));
    }

    // Spring MVC가 관리하는 400·404·405 등은 원래 상태코드를 보존한다.
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request
    ) {
        String message;
        if (statusCode.value() == 404) {
            message = "요청한 리소스를 찾을 수 없습니다.";
        } else if (statusCode.value() == 405) {
            message = "허용되지 않은 요청 방식입니다.";
        } else if (statusCode.is4xxClientError()) {
            message = "요청 정보를 확인해 주세요.";
        } else {
            message = "서버에서 요청을 처리하지 못했습니다.";
        }

        return new ResponseEntity<>(
                Map.of(
                        "error", "HTTP_" + statusCode.value(),
                        "message", message
                ),
                headers,
                statusCode
        );
    }

    // 상태코드가 명시되지 않은 예외는 내부 내용을 노출하지 않고 500으로 응답한다.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpectedException(
            Exception e) {
        log.error("요청 처리 중 예상하지 못한 오류가 발생했습니다.", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "INTERNAL_SERVER_ERROR",
                        "message", "서버에서 요청을 처리하지 못했습니다."
                ));
    }
}