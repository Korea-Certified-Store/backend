package com.nainga.nainga.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/*
    가게와 관련된 도메인에서 발생할 수 있는 ErrorCode를 정의합니다.
 */
@Getter
@RequiredArgsConstructor
public enum StoreErrorCode implements ErrorCode {
    INVALID_FILE_EXTENSION(HttpStatus.NOT_FOUND, "올바르지 않은 파일 확장자입니다. Excel 파일 확장자만 허용됩니다."),
    GCS_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Google Cloud Storage 관련 서버 오류입니다."),
    GOOGLE_MAP_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Google Map API 관련 서버 오류입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
