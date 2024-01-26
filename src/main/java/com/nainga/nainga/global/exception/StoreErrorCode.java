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
    INVALID_FILE_EXTENSION(HttpStatus.NOT_FOUND, "There are incorrect files. Only the extension of Excel file is allowed."),
    GCS_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "There are internal server errors related to Google Cloud Storage."),
    GOOGLE_MAP_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "There are internal server errors related to Google Map API.");

    private final HttpStatus httpStatus;
    private final String message;
}
