package com.nainga.nainga.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/*
    공통적으로 사용될 수 있는 ErrorCode를 정의합니다.
 */
@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "유효하지 않은 파라미터입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청하신 리소스가 존재하지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "내부적인 서버 오류입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
