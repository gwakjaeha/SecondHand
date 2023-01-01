package com.example.secondhand.global.exception;

import static com.example.secondhand.global.exception.CustomErrorCode.INTERNAL_SERVER_ERROR;
import static com.example.secondhand.global.exception.CustomErrorCode.INVALID_REQUEST;

import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice // 각 컨트롤러에 advice 역할을 하는 어노테이션, 빈 등록 포함, json 형태로 에러 메시지를 보여줌

public class CustomExceptionHandler {

    // 글로벌 예외처리
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CustomException.class)
    public CustomErrorResponse handleException(
            CustomException e,
            HttpServletRequest request
    ) {
        log.error("errorCode : {}, url {}, message: {}",
                e.getCustomErrorCode(), request.getRequestURI(), e.getDetaliMessage());

        return CustomErrorResponse.builder()
                .status(e.getCustomErrorCode())
                .statusMessage(e.getDetaliMessage())
                .build();
    }

    // 예외처리 하기 힘든 예외처리
    @ExceptionHandler(value = {
            HttpRequestMethodNotSupportedException.class, // get, post 등 메소드(요청)가 매치하지 않았을경우
            MethodArgumentNotValidException.class, // 컨트롤러 내부 진입 전에 밸리데이션으로 발생하는 에러를 잡음
    })
    public CustomErrorResponse handleBadRequest(
            Exception e, HttpServletRequest request
    ) {
        log.error("url {}, message: {}",
                request.getRequestURI(), e.getMessage());

        return CustomErrorResponse.builder()
                .status(INVALID_REQUEST)
                .statusMessage(INVALID_REQUEST.getStatusMessage())
                .build();
    }

    // 알수없거나 알아내기 힘들 오류의 최후 처리
    @ExceptionHandler(Exception.class)
    public CustomErrorResponse handleException(
            Exception e, HttpServletRequest request
    ) {
        log.error("url {}, message: {}",
                request.getRequestURI(), e.getMessage());

        return CustomErrorResponse.builder()
                .status(INTERNAL_SERVER_ERROR)
                .statusMessage(INVALID_REQUEST.getStatusMessage())
                .build();
    }
}
