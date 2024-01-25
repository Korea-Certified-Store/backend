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
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "There are invalid parameters."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Requested resources are not found."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "There are internal server errors.");

    private final HttpStatus httpStatus;
    private final String message;
}
