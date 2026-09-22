package com.project.articket.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(FileUploadException.class)
    public String handlerFileUploadException(FileUploadException fileUploadException
    , Model model){
        log.error("파일 업로드 처리중 오류 발생",fileUploadException);

        model.addAttribute("errorMessage","파일 업로드 실패");

        return "error/file-upload-error";
    }
}
