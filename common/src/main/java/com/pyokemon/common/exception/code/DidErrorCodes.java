package com.pyokemon.common.exception.code;

public final class DidErrorCodes {

  // 지갑 관련 에러
  public static final String WALLET_ALREADY_EXISTS = "WALLET_ALREADY_EXISTS";
  public static final String WALLET_CREATION_FAILED = "WALLET_CREATION_FAILED";
  public static final String WALLET_NOT_FOUND = "WALLET_NOT_FOUND";

  // Public DID 관련 에러
  public static final String DID_CREATION_FAILED = "DID_CREATION_FAILED";
  public static final String DID_NOT_FOUND = "DID_NOT_FOUND";

  // 초대장 관련 에러
  public static final String INVITATION_CREATION_FAILED = "INVITATION_CREATION_FAILED";
  public static final String INVITATION_INVALID = "INVITATION_INVALID";
  public static final String INVITATION_ALREADY_SENT = "INVITATION_ALREADY_SENT";


  // Connection 관련 에러
  public static final String CONNECTION_CREATION_FAILED = "CONNECTION_CREATION_FAILED";
  public static final String CONNECTION_NOT_FOUND = "CONNECTION_NOT_FOUND";
  public static final String CONNECTION_ALREADY_ACTIVE = "CONNECTION_ALREADY_ACTIVE";
  public static final String CONNECTION_INVALID_STATE = "CONNECTION_INVALID_STATE";

  // Webhook 관련 에러
  public static final String WEBHOOK_PROCESSING_FAILED = "WEBHOOK_PROCESSING_FAILED";
  public static final String WEBHOOK_INVALID_PAYLOAD = "WEBHOOK_INVALID_PAYLOAD";
  public static final String WEBHOOK_TIMEOUT = "WEBHOOK_TIMEOUT";

  // VC 관련 에러
  public static final String VC_ISSUANCE_FAILED = "VC_ISSUANCE_FAILED";
  public static final String VC_NOT_FOUND = "VC_NOT_FOUND";
  public static final String VC_ALREADY_CONSUMED = "VC_ALREADY_CONSUMED";

  // VP 검증 관련 에러
  public static final String VP_VERIFICATION_FAILED = "VP_VERIFICATION_FAILED";
  public static final String VP_CHALLENGE_MISMATCH = "VP_CHALLENGE_MISMATCH";

  // ACA-Py 관련 에러
  public static final String ACAPY_SERVICE_ERROR = "ACAPY_SERVICE_ERROR";
  public static final String ACAPY_SERVICE_UNAVAILABLE = "ACAPY_SERVICE_UNAVAILABLE";

  // 인증 관련 에러
  public static final String ACCESS_DENIED = "ACCESS_DENIED";
  public static final String PERMISSION_DENIED = "PERMISSION_DENIED";

  // 일반적인 에러
  public static final String INVALID_REQUEST = "INVALID_REQUEST";
  public static final String DATABASE_ERROR = "DATABASE_ERROR";
  public static final String EVENT_PROCESSING_FAILED = "EVENT_PROCESSING_FAILED";

}
