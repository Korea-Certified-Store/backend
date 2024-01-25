package com.nainga.nainga.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.FieldError;

import java.util.List;

/*
    Error 발생 시 응답에 대한 클래스입니다.
 */
@Getter
@Builder
@RequiredArgsConstructor
public class ErrorResponse {
    private final int httpStatusValue;
    private final String httpStatusCode;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)     //만약 비어있으면 JSON에 포함되지 않도록 JsonInclude 애너테이션을 붙였습니다.
    private final List<ValidationError> errors;    //Validation Error 발생 시 오류 정보들을 저장합니다.

    @Getter
    @Builder
    @RequiredArgsConstructor
    public static class ValidationError {   //@Valid와 같은 애너테이션을 사용 시 에러가 발생했을 때 어떤 필드에서 에러가 발생했는지 알 수 있도록 정적 클래스를 추가
        private final String field;
        private final String message;

        public static ValidationError of(final FieldError fieldError) {
            return ValidationError.builder()
                    .field(fieldError.getField())
                    .message(fieldError.getDefaultMessage())
                    .build();
        }
    }
}
