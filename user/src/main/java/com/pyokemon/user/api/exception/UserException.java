package com.pyokemon.user.api.exception;

import com.pyokemon.common.exception.BusinessException;

// 테넌트 예외
public class UserException extends BusinessException {

    // 기존 방식 (호환성 유지)
    public UserException(String message, String errorCode) {
        super(message, errorCode);
    }

    public UserException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }

    // 편의 메서드들

    // 테넌트를 찾을 수 없는 경우
    public static UserException notFound() {
        return new UserException("존재하지 않는 사용자입니다", "USER_NOT_FOUND");
    }

    // login 실패
    public static UserException loginFailed() {
        return new UserException("아이디 또는 비밀번호가 올바르지 않습니다", "LOGIN_FAILED");
    }

    // 접근 권한 없음
    public static UserException accessDenied() { return new UserException("접근 권한이 없습니다", "ACCESS_DENIED"); }

    // 중복 이메일
    public static UserException duplicateEmail(String email) { return new UserException("이미 존재하는 이메일입니다: " + email, "DUPLICATE_EMAIL");}
}
