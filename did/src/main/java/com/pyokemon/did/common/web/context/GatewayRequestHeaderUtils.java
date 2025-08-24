package com.pyokemon.did.common.web.context;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.DidErrorCodes;

/**
 * DID 서비스의 /api 경로에서 Gateway를 통해 전달받은 헤더 정보를 안전하게 추출하는 유틸리티 클래스 /api 경로는 Gateway를 거쳐서 사용자 인증 정보를
 * 받아옵니다.
 */
public class GatewayRequestHeaderUtils {

  /**
   * Gateway에서 전달받은 사용자 ID를 Long 타입으로 반환하거나 예외를 발생시킵니다.
   * 
   * @return 사용자 ID (Long)
   * @throws BusinessException 인증 정보가 없거나 숫자 형식이 아닌 경우
   */
  public static Long getUserIdOrThrowException() {
    HttpServletRequest request = getCurrentRequest();
    String accountId = request.getHeader("X-Auth-AccountId");
    if (accountId == null || accountId.isEmpty()) {
      throw new BusinessException("사용자 인증 정보가 없습니다.", DidErrorCodes.ACCESS_DENIED);
    }

    try {
      return Long.valueOf(accountId);
    } catch (NumberFormatException e) {
      throw new BusinessException("사용자 ID가 올바른 숫자 형식이 아닙니다: " + accountId,
          DidErrorCodes.ACCESS_DENIED);
    }
  }

  /**
   * Gateway에서 전달받은 사용자 역할을 반환하거나 예외를 발생시킵니다.
   * 
   * @return 사용자 역할
   * @throws BusinessException 권한 정보가 없는 경우
   */
  public static String getUserRoleOrThrowException() {
    HttpServletRequest request = getCurrentRequest();
    String role = request.getHeader("X-Auth-Role");
    if (role == null || role.isEmpty()) {
      throw new BusinessException("사용자 권한 정보가 없습니다.", DidErrorCodes.ACCESS_DENIED);
    }
    return role;
  }

  /**
   * 사용자가 테넌트 권한을 가지고 있는지 확인합니다.
   * 
   * @return 테넌트 권한 여부
   */
  public static boolean isTenant() {
    String role = getUserRoleOrThrowException();
    return "TENANT".equals(role);
  }

  /**
   * 사용자가 일반 사용자 권한을 가지고 있는지 확인합니다.
   * 
   * @return 일반 사용자 권한 여부
   */
  public static boolean isUser() {
    String role = getUserRoleOrThrowException();
    return "USER".equals(role);
  }

  /**
   * 사용자가 관리자 권한을 가지고 있는지 확인합니다.
   * 
   * @return 관리자 권한 여부
   */
  public static boolean isAdmin() {
    String role = getUserRoleOrThrowException();
    return "ADMIN".equals(role);
  }

  /**
   * Gateway에서 전달받은 사용자 ID를 반환합니다. role이 USER인 경우에만 반환합니다.
   * 
   * @return 사용자 ID
   * @throws BusinessException role이 USER가 아닌 경우
   */
  public static String getAccountId() {
    HttpServletRequest request = getCurrentRequest();
    String accountId = request.getHeader("X-Auth-AccountId");

    if (accountId == null || accountId.isEmpty()) {
      throw new BusinessException("사용자 인증 정보가 없습니다.", DidErrorCodes.ACCESS_DENIED);
    }

    // role이 USER인지 확인
    String role = getUserRoleOrThrowException();
    if (!"USER".equals(role)) {
      throw new BusinessException("일반 사용자만 접근할 수 있습니다.", DidErrorCodes.PERMISSION_DENIED);
    }

    return accountId;
  }

  /**
   * Gateway에서 전달받은 클라이언트 디바이스 정보를 반환합니다.
   * 
   * @return 클라이언트 디바이스 (MOBILE, TABLET, WEB, API_CLIENT, unknown)
   */
  public static String getClientDevice() {
    HttpServletRequest request = getCurrentRequest();
    String device = request.getHeader("X-Client-Device");
    return device != null ? device : "unknown";
  }

  /**
   * 현재 HTTP 요청 객체를 반환합니다.
   * 
   * @return HttpServletRequest
   * @throws BusinessException 요청 컨텍스트를 찾을 수 없는 경우
   */
  private static HttpServletRequest getCurrentRequest() {
    ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
    if (attributes == null) {
      throw new BusinessException("요청 컨텍스트를 찾을 수 없습니다.", DidErrorCodes.DATABASE_ERROR);
    }
    return attributes.getRequest();
  }
}
