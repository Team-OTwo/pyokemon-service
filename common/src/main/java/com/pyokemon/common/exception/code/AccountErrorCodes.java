package com.pyokemon.common.exception.code;

// Account error code
public final class AccountErrorCodes {

  // 공통 계정 관련 에러
  public static final String ACCOUNT_NOT_FOUND = "ACCOUNT_NOT_FOUND";
  public static final String ACCOUNT_DELETED = "ACCOUNT_DELETED";
  public static final String ACCOUNT_LOCKED = "ACCOUNT_LOCKED";
  public static final String ACCOUNT_DISABLED = "ACCOUNT_DISABLED";
  public static final String ACCOUNT_NOT_VERIFIED = "ACCOUNT_NOT_VERIFIED";

  // 인증 관련 에러
  public static final String LOGIN_FAILED = "LOGIN_FAILED";
  public static final String INVALID_LOGIN = "INVALID_LOGIN";
  public static final String PASSWORD_MATCH = "PASSWORD_MATCH";
  public static final String PASSWORD_MISMATCH = "PASSWORD_MISMATCH";
  public static final String TOKEN_EXPIRED = "TOKEN_EXPIRED";
  public static final String TOKEN_INVALID = "TOKEN_INVALID";
  public static final String INVALID_TOKEN = "INVALID_TOKEN";
  public static final String ACCESS_DENIED = "ACCESS_DENIED";
  public static final String PERMISSION_DENIED = "PERMISSION_DENIED";

  // 중복 관련 에러
  public static final String LOGIN_ID_DUPLICATED = "LOGIN_ID_DUPLICATED";
  public static final String DUPLICATE_LOGIN_ID = "DUPLICATE_LOGIN_ID";
  public static final String DUPLICATE_CORP_ID = "DUPLICATE_CORP_ID";
  public static final String EMAIL_DUPLICATED = "EMAIL_DUPLICATED";
  public static final String PHONE_DUPLICATED = "PHONE_DUPLICATED";

  // 필수 필드 관련 에러
  public static final String LOGIN_ID_REQUIRED = "LOGIN_ID_REQUIRED";
  public static final String PASSWORD_REQUIRED = "PASSWORD_REQUIRED";
  public static final String EMAIL_REQUIRED = "EMAIL_REQUIRED";
  public static final String NAME_REQUIRED = "NAME_REQUIRED";
  public static final String PHONE_REQUIRED = "PHONE_REQUIRED";

  // 테넌트 관련 에러
  public static final String TENANT_NOT_FOUND = "TENANT_NOT_FOUND";

  // 유저 관련 에러
  public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
  public static final String USER_ALREADY_VERIFIED = "USER_ALREADY_VERIFIED";

  // 시스템 에러
  public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
  public static final String SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";
  public static final String DATABASE_ERROR = "DATABASE_ERROR";

  // 디바이스 에러
  public static final String DEVICE_NOT_FOUND = "DEVICE_NOT_FOUND";
  public static final String DEVICE_ALREADY_REGISTERED = "DEVICE_ALREADY_REGISTERED";

  private AccountErrorCodes() {
    throw new UnsupportedOperationException("상수 클래스는 인스턴스를 생성할 수 없습니다");
  }
}
