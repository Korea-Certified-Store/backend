package com.nainga.nainga.global.util;

import lombok.AllArgsConstructor;
import lombok.Data;

/*
이 Result 유틸은 api 응답 반환 시 한번 wrapping해서 반환해주기 위해 사용
 */
@Data
@AllArgsConstructor
public class Result<T> {
    public static final String MESSAGE_OK = "ok";

    public static final int CODE_CONTINUE = 100;
    public static final int CODE_SUCCESS = 200;
    public static final int CODE_REDIRECT = 300;
    public static final int CODE_CLIENT_ERROR = 400;
    public static final int CODE_SERVER_ERROR = 500;

    private int code;
    private String message;
    private T data;
}
