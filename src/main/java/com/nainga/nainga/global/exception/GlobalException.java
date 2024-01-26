package com.nainga.nainga.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/*
    전체적으로 발생하는 예외를 처리해줄 예외 클래스
 */
@Getter
@RequiredArgsConstructor
public class GlobalException extends RuntimeException {
    private final ErrorCode errorCode;
}
