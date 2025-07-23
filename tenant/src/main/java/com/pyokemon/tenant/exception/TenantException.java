package com.pyokemon.tenant.exception;

import com.pyokemon.common.exception.BusinessException;

//테넌트 예외
public class TenantException extends BusinessException {

    public TenantException(String message, String errorCode) {
        super(message, errorCode);
    }

    public TenantException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }

    // 자주 쓰는 몇 개만 편의 메서드로 제공

    //테넌트를 찾을 수 없는 경우
    public static TenantException notFound() {
        return new TenantException("존재하지 않는 테넌트입니다", "TENANT_NOT_FOUND");
    }

    //login 실패
    public static TenantException loginFailed() {
        return new TenantException("아이디 또는 비밀번호가 올바르지 않습니다", "LOGIN_FAILED");
    }
} 