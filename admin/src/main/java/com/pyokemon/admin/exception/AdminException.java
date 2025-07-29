package com.pyokemon.admin.exception;

import com.pyokemon.common.exception.BusinessException;

// 관리자 예외
public class AdminException extends BusinessException {

  // 기존 방식 (호환성 유지)
  public AdminException(String message, String errorCode) {
    super(message, errorCode);
  }

  public AdminException(String message, String errorCode, Throwable cause) {
    super(message, errorCode, cause);
  }

  // 편의 메서드들

  // 관리자를 찾을 수 없는 경우
  public static AdminException notFound() {
    return new AdminException("존재하지 않는 관리자입니다", "ADMIN_NOT_FOUND");
  }

  // login 실패
  public static AdminException loginFailed() {
    return new AdminException("아이디 또는 비밀번호가 올바르지 않습니다", "ADMIN_LOGIN_FAILED");
  }

  // 접근 권한 없음
  public static AdminException accessDenied() {
    return new AdminException("접근 권한이 없습니다", "ADMIN_ACCESS_DENIED");
  }

  // 비밀번호 불일치
  public static AdminException passwordMismatch() {
    return new AdminException("비밀번호가 일치하지 않습니다", "ADMIN_PASSWORD_MISMATCH");
  }

  // 이미 존재하는 관리자
  public static AdminException alreadyExists() {
    return new AdminException("이미 존재하는 관리자입니다", "ADMIN_ALREADY_EXISTS");
  }

  // 토큰 만료
  public static AdminException tokenExpired() {
    return new AdminException("토큰이 만료되었습니다", "ADMIN_TOKEN_EXPIRED");
  }

  // 유효하지 않은 토큰
  public static AdminException invalidToken() {
    return new AdminException("유효하지 않은 토큰입니다", "ADMIN_TOKEN_INVALID");
  }

  // 이벤트 승인 관련 예외
  public static AdminException eventApprovalFailed() {
    return new AdminException("이벤트 승인에 실패했습니다", "ADMIN_EVENT_APPROVAL_FAILED");
  }
}
