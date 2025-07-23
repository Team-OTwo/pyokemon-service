package com.pyokemon.user.api.exception;

import com.pyokemon.common.exception.BusinessException;

public class InvalidPasswordException extends BusinessException {
    private static final String ERROR_CODE = "INVALID_PASSWORD";
    
    public InvalidPasswordException(String message) {
        super(message, ERROR_CODE);
    }
} 