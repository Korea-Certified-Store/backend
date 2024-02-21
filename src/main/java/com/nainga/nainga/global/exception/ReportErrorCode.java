package com.nainga.nainga.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/*
    Report와 관련된 도메인에서 발생할 수 있는 ErrorCode를 정의합니다.
 */
@Getter
@RequiredArgsConstructor
public enum ReportErrorCode implements ErrorCode {
    INVALID_DTYPE(HttpStatus.NOT_FOUND, "There is a wrong dtype. You can only use a dtype such as fix or del."),
    INVALID_CERTIFICATION(HttpStatus.NOT_FOUND, "There is a wrong certification. You can only use certifications such as 착한가격업소, 모범음식점, 안심식당."),
    INVALID_REPORT_ID(HttpStatus.NOT_FOUND, "There is a wrong reportId.");

    private final HttpStatus httpStatus;
    private final String message;
}
