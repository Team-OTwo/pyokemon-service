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

}
