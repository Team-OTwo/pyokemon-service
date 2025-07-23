package com.pyokemon.tenant.exception;

import com.pyokemon.common.exception.BusinessException;

// 테넌트 예외
public class TenantException extends BusinessException {

  // 기존 방식 (호환성 유지)
  public TenantException(String message, String errorCode) {
    super(message, errorCode);
  }

  public TenantException(String message, String errorCode, Throwable cause) {
    super(message, errorCode, cause);
  }

  // 편의 메서드들

  // 테넌트를 찾을 수 없는 경우
  public static TenantException notFound() {
    return new TenantException("존재하지 않는 테넌트입니다", "TENANT_NOT_FOUND");
  }

  // login 실패
  public static TenantException loginFailed() {
    return new TenantException("아이디 또는 비밀번호가 올바르지 않습니다", "LOGIN_FAILED");
  }

  // 접근 권한 없음
  public static TenantException accessDenied() {
    return new TenantException("접근 권한이 없습니다", "ACCESS_DENIED");
  }
}
