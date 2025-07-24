package com.pyokemon.common.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TenantErrorCode {

  // 공통 에러
  TENANT_ID_REQUIRED("테넌트 ID는 필수입니다"), TENANT_NOT_FOUND("존재하지 않는 테넌트입니다"), ACCESS_DENIED(
      "접근 권한이 없습니다"),

  // 테넌트 생성/수정 관련
  LOGIN_ID_REQUIRED("아이디는 필수입니다"), LOGIN_ID_DUPLICATED(
      "이미 존재하는 아이디입니다"), BUSINESS_NUMBER_DUPLICATED("이미 등록된 사업자번호입니다"),

  // 로그인 관련
  PASSWORD_REQUIRED("비밀번호는 필수입니다"), LOGIN_FAILED("아이디 또는 비밀번호가 올바르지 않습니다"),

  // 비밀번호 변경 관련
  OLD_PASSWORD_REQUIRED("현재 비밀번호는 필수입니다"), NEW_PASSWORD_REQUIRED(
      "새 비밀번호는 필수입니다"), CURRENT_PASSWORD_MISMATCH("현재 비밀번호가 일치하지 않습니다"),

  // 토큰 관련
  REFRESH_TOKEN_REQUIRED("리프레시 토큰은 필수입니다"), INVALID_REFRESH_TOKEN(
      "유효하지 않은 리프레시 토큰입니다"), AUTHORIZATION_HEADER_MISSING(
          "Authorization 헤더가 없습니다"), INVALID_AUTHORIZATION_HEADER(
              "Bearer 토큰이 아닙니다"), EMPTY_TOKEN("토큰이 비어있습니다"),

  // 요청 헤더 관련
  USER_ROLE_REQUIRED("사용자 권한 정보가 없습니다"), CLIENT_DEVICE_REQUIRED(
      "클라이언트 디바이스 정보가 없습니다"), CLIENT_ADDRESS_REQUIRED("클라이언트 IP 정보가 없습니다"),

  // 기타
  USER_NOT_FOUND("존재하지 않는 사용자입니다"), TENANT_DELETE_FAILED("테넌트 삭제에 실패했습니다");

  private final String message;

  public String getCode() {
    return this.name();
  }
}
