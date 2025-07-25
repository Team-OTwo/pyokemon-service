package com.pyokemon.common.exception.code;

//Event error code

public final class TenantErrorCodes {

    public static final String TENANT_NOT_FOUND = "TENANT_NOT_FOUND";
    public static final String TENANT_LOGIN_FAILED = "TENANT_LOGIN_FAILED";
    public static final String TENANT_ACCESS_DENIED = "TENANT_ACCESS_DENIED";
    public static final String TENANT_TOKEN_EXPIRED = "TENANT_TOKEN_EXPIRED";
    public static final String TENANT_TOKEN_INVALID = "TENANT_TOKEN_INVALID";

    public static final String TENANT_ALREADY_EXISTS = "TENANT_ALREADY_EXISTS";
    public static final String TENANT_LOGIN_ID_DUPLICATED = "TENANT_LOGIN_ID_DUPLICATED";
    public static final String TENANT_EMAIL_DUPLICATED = "TENANT_EMAIL_DUPLICATED";
    public static final String TENANT_PASSWORD_MISMATCH = "TENANT_PASSWORD_MISMATCH";
    public static final String TENANT_ACCOUNT_LOCKED = "TENANT_ACCOUNT_LOCKED";
    public static final String TENANT_ACCOUNT_DISABLED = "TENANT_ACCOUNT_DISABLED";

    public static final String TENANT_ID_REQUIRED = "TENANT_ID_REQUIRED";
    public static final String TENANT_LOGIN_ID_REQUIRED = "TENANT_LOGIN_ID_REQUIRED";
    public static final String TENANT_PASSWORD_REQUIRED = "TENANT_PASSWORD_REQUIRED";
    public static final String TENANT_EMAIL_REQUIRED = "TENANT_EMAIL_REQUIRED";
    public static final String TENANT_NAME_REQUIRED = "TENANT_NAME_REQUIRED";
    public static final String TENANT_PHONE_REQUIRED = "TENANT_PHONE_REQUIRED";

    public static final String TENANT_COMPANY_NOT_FOUND = "TENANT_COMPANY_NOT_FOUND";
    public static final String TENANT_PERMISSION_DENIED = "TENANT_PERMISSION_DENIED";
    public static final String TENANT_EVENT_LIMIT_EXCEEDED = "TENANT_EVENT_LIMIT_EXCEEDED";
    public static final String TENANT_SUBSCRIPTION_EXPIRED = "TENANT_SUBSCRIPTION_EXPIRED";

    public static final String TENANT_INTERNAL_ERROR = "TENANT_INTERNAL_ERROR";
    public static final String TENANT_SERVICE_UNAVAILABLE = "TENANT_SERVICE_UNAVAILABLE";
    public static final String TENANT_DATABASE_ERROR = "TENANT_DATABASE_ERROR";

    private TenantErrorCodes() {
        throw new UnsupportedOperationException("상수 클래스는 인스턴스를 생성할 수 없습니다");
    }
} 