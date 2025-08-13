package com.pyokemon.common.exception.code;

public final class DidErrorCodes {
    // 지갑 관련 에러
    public static final String WALLET_ALREADY_EXISTS = "WALLET_ALREADY_EXISTS";
    public static final String WALLET_CREATION_FAILED = "WALLET_CREATION_FAILED";
    public static final String WALLET_NOTFOUND = "WALLET_NOTFOUND";
    
    // 초대장 관련 에러
    public static final String INVITATION_CREATION_FAILED = "INVITATION_CREATION_FAILED";
    
    // 입력값 검증 관련 에러
    public static final String INVALID_REQUEST = "INVALID_REQUEST";
    
    // 데이터베이스 관련 에러
    public static final String DATABASE_ERROR = "DATABASE_ERROR";
    
    // AcaPy 서비스 관련 에러
    public static final String ACAPY_SERVICE_ERROR = "ACAPY_SERVICE_ERROR";
}
