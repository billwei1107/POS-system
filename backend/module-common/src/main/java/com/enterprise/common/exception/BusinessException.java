package com.enterprise.common.exception;

import lombok.Getter;

/**
 * @file BusinessException.java
 * @description 自訂業務例外類 / Custom business exception
 */
@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }
}
