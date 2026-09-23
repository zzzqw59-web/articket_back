package com.project.articket.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(FileUploadException.class)
    public String handleFileUploadException(FileUploadException fileUploadException, Model model) {
        log.error("파일 업로드 처리 중 오류가 발생했습니다.", fileUploadException);

        model.addAttribute("errorMessage", "파일 업로드에 실패했습니다.");

        return "error/file-upload-error";
    }
}
