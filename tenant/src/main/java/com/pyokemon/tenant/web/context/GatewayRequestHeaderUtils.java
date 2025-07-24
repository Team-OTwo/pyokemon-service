package com.pyokemon.tenant.web.context;

import com.pyokemon.common.exception.code.TenantErrorCode;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.pyokemon.common.exception.BusinessException;

// Gateway에서 전달한 요청 헤더 정보를 쉽게 꺼내기 위한 유틸 클래스
public class GatewayRequestHeaderUtils {

  private static final String BEARER_PREFIX = "Bearer ";

  // 요청 헤더에서 특정 키의 값을 문자열로 가져옴
  public static String getRequestHeaderParamAsString(String key) {
    // 현재 쓰레드의 요청 정보를 가져옴 (Spring의 RequestContextHolder 사용)
    ServletRequestAttributes requestAttributes =
        (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
    // 요청 객체에서 헤더 값 추출
    return requestAttributes.getRequest().getHeader(key);
  }

  // 사용자 ID 가져오기
  public static String getUserId() {
    return getRequestHeaderParamAsString("X-Auth-UserId");
  }

  // 사용자 역할 가져오기
  public static String getUserRole() {
    return getRequestHeaderParamAsString("X-User-Role");
  }

  // Authorization 헤더 가져오기
  public static String getAuthorizationHeader() {
    return getRequestHeaderParamAsString("Authorization");
  }

  // Bearer 토큰 추출 (Bearer 접두사 제거)
  public static String getBearerToken() {
    String authHeader = getAuthorizationHeader();
    if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      return null;
    }
    String token = authHeader.substring(BEARER_PREFIX.length()).trim();
    return token.isEmpty() ? null : token;
  }

  // 클라이언트 디바이스 정보 가져오기 (ex: WEB, MOBILE 등)
  public static String getClientDevice() {
    return getRequestHeaderParamAsString("X-Client-Device");
  }

  // 클라이언트 IP 주소 가져오기
  public static String getClientAddress() {
    return getRequestHeaderParamAsString("X-Client-Address");
  }

  // 사용자 ID가 없으면 예외를 던지도록 강제
  public static String getUserIdOrThrowException() {
    String userId = getUserId();
    if (userId == null || userId.trim().isEmpty()) {
      throw new BusinessException(TenantErrorCode.ACCESS_DENIED.getMessage(), TenantErrorCode.ACCESS_DENIED.getCode());
    }
    return userId;
  }

  // 사용자 역할이 없으면 예외 던짐
  public static String getUserRoleOrThrowException() {
    String userRole = getUserRole();
    if (userRole == null || userRole.trim().isEmpty()) {
      throw new BusinessException(TenantErrorCode.USER_ROLE_REQUIRED.getMessage(), TenantErrorCode.USER_ROLE_REQUIRED.getCode());
    }
    return userRole;
  }

  // Bearer 토큰이 없으면 예외 던짐
  public static String getBearerTokenOrThrowException() {
    String authHeader = getAuthorizationHeader();

    // Authorization 헤더 체크
    if (authHeader == null || authHeader.trim().isEmpty()) {
      throw new BusinessException(TenantErrorCode.AUTHORIZATION_HEADER_MISSING.getMessage(), TenantErrorCode.AUTHORIZATION_HEADER_MISSING.getCode());
    }

    // Bearer 접두사 체크
    if (!authHeader.startsWith(BEARER_PREFIX)) {
      throw new BusinessException(TenantErrorCode.INVALID_AUTHORIZATION_HEADER.getMessage(), TenantErrorCode.INVALID_AUTHORIZATION_HEADER.getCode());
    }

    // 토큰 추출 및 검증
    String token = authHeader.substring(BEARER_PREFIX.length()).trim();
    if (token.isEmpty()) {
      throw new BusinessException(TenantErrorCode.EMPTY_TOKEN.getMessage(), TenantErrorCode.EMPTY_TOKEN.getCode());
    }

    return token;
  }

  // 디바이스 정보가 없으면 예외 던짐
  public static String getClientDeviceOrThrowException() {
    String clientDevice = getClientDevice();
    if (clientDevice == null || clientDevice.trim().isEmpty()) {
      throw new BusinessException(TenantErrorCode.CLIENT_DEVICE_REQUIRED.getMessage(), TenantErrorCode.CLIENT_DEVICE_REQUIRED.getCode());
    }
    return clientDevice;
  }

  // IP 주소가 없으면 예외 던짐
  public static String getClientAddressOrThrowException() {
    String clientAddress = getClientAddress();
    if (clientAddress == null || clientAddress.trim().isEmpty()) {
      throw new BusinessException(TenantErrorCode.CLIENT_ADDRESS_REQUIRED.getMessage(), TenantErrorCode.CLIENT_ADDRESS_REQUIRED.getCode());
    }
    return clientAddress;
  }

  // 관리자 권한인지 체크
  public static boolean isAdmin() {
    String userRole = getUserRole();
    return "ADMIN".equals(userRole);
  }

  // 테넌트 권한인지 체크
  public static boolean isTenant() {
    String userRole = getUserRole();
    return "TENANT".equals(userRole);
  }

  // 일반 사용자 권한인지 체크
  public static boolean isUser() {
    String userRole = getUserRole();
    return "USER".equals(userRole);
  }

  // 관리자 권한이 아니면 예외 던짐
  public static void requireAdminRole() {
    if (!isAdmin()) {
      throw new BusinessException(TenantErrorCode.ACCESS_DENIED.getMessage(), TenantErrorCode.ACCESS_DENIED.getCode());
    }
  }

  // 테넌트 권한이 아니면 예외 던짐
  public static void requireTenantRole() {
    if (!isTenant()) {
      throw new BusinessException(TenantErrorCode.ACCESS_DENIED.getMessage(), TenantErrorCode.ACCESS_DENIED.getCode());
    }
  }
}
