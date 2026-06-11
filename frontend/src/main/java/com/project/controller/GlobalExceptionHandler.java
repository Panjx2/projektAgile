package com.project.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView handleMaxUploadSize(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        String redirectUrl = referer != null ? referer : "/";
        if (!redirectUrl.contains("uploadError")) {
            redirectUrl += (redirectUrl.contains("?") ? "&" : "?") + "uploadError=fileTooLarge";
        }
        return new ModelAndView("redirect:" + redirectUrl);
    }
}
