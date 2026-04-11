package com.enterprise.common.exception;

/**
 * @file ResourceNotFoundException.java
 * @description 資源找不到例外類 / Resource not found exception
 */
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String message) {
        super(404, message);
    }
}
