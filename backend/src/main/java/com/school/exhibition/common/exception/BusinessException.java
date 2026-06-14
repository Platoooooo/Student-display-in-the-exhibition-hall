package com.school.exhibition.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(String msg) {
        this(500, msg);
    }

    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
    }
}
