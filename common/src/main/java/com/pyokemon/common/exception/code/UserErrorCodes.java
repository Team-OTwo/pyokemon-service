package com.pyokemon.common.exception.code;

//User error code 예시 - 서비스 맡으신 분이 사용하면서 수정하시면 됩니다 ~!
public final class UserErrorCodes {

    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String USER_LOGIN_FAILED = "USER_LOGIN_FAILED";
    public static final String USER_ACCESS_DENIED = "USER_ACCESS_DENIED";
    public static final String USER_TOKEN_EXPIRED = "USER_TOKEN_EXPIRED";
    public static final String USER_TOKEN_INVALID = "USER_TOKEN_INVALID";

    public static final String USER_ALREADY_EXISTS = "USER_ALREADY_EXISTS";
    public static final String USER_EMAIL_DUPLICATED = "USER_EMAIL_DUPLICATED";
    public static final String USER_PHONE_DUPLICATED = "USER_PHONE_DUPLICATED";
    public static final String USER_PASSWORD_MISMATCH = "USER_PASSWORD_MISMATCH";
    public static final String USER_ACCOUNT_LOCKED = "USER_ACCOUNT_LOCKED";
    public static final String USER_ACCOUNT_DISABLED = "USER_ACCOUNT_DISABLED";
    public static final String USER_ACCOUNT_NOT_VERIFIED = "USER_ACCOUNT_NOT_VERIFIED";

    public static final String DEVICE_NOT_FOUND = "DEVICE_NOT_FOUND";
    public static final String DEVICE_ALREADY_REGISTERED = "DEVICE_ALREADY_REGISTERED";
    public static final String DEVICE_REGISTRATION_FAILED = "DEVICE_REGISTRATION_FAILED";
    public static final String DEVICE_AUTHENTICATION_FAILED = "DEVICE_AUTHENTICATION_FAILED";
    public static final String DEVICE_LIMIT_EXCEEDED = "DEVICE_LIMIT_EXCEEDED";
    public static final String DEVICE_TOKEN_INVALID = "DEVICE_TOKEN_INVALID";

    public static final String USER_ID_REQUIRED = "USER_ID_REQUIRED";
    public static final String USER_EMAIL_REQUIRED = "USER_EMAIL_REQUIRED";
    public static final String USER_PASSWORD_REQUIRED = "USER_PASSWORD_REQUIRED";
    public static final String USER_NAME_REQUIRED = "USER_NAME_REQUIRED";
    public static final String USER_PHONE_REQUIRED = "USER_PHONE_REQUIRED";
    public static final String USER_BIRTH_DATE_REQUIRED = "USER_BIRTH_DATE_REQUIRED";

    public static final String FAVORITE_EVENT_NOT_FOUND = "FAVORITE_EVENT_NOT_FOUND";
    public static final String FAVORITE_EVENT_ALREADY_EXISTS = "FAVORITE_EVENT_ALREADY_EXISTS";
    public static final String FAVORITE_EVENT_LIMIT_EXCEEDED = "FAVORITE_EVENT_LIMIT_EXCEEDED";

    public static final String PROFILE_UPDATE_FAILED = "PROFILE_UPDATE_FAILED";
    public static final String PROFILE_IMAGE_UPLOAD_FAILED = "PROFILE_IMAGE_UPLOAD_FAILED";
    public static final String PROFILE_IMAGE_SIZE_EXCEEDED = "PROFILE_IMAGE_SIZE_EXCEEDED";

    public static final String USER_INTERNAL_ERROR = "USER_INTERNAL_ERROR";
    public static final String USER_SERVICE_UNAVAILABLE = "USER_SERVICE_UNAVAILABLE";
    public static final String USER_DATABASE_ERROR = "USER_DATABASE_ERROR";

    private UserErrorCodes() {
        throw new UnsupportedOperationException("상수 클래스는 인스턴스를 생성할 수 없습니다");
    }
} 