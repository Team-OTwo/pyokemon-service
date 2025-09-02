package com.pyokemon.common.web.context;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.DidErrorCodes;
import com.pyokemon.common.web.constant.GatewayHeaderConstants;

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
    String accountId = request.getHeader(GatewayHeaderConstants.Auth.X_AUTH_ACCOUNT_ID);
    if (accountId == null || accountId.isEmpty()) {
      throw new BusinessException("사용자 인증 정보가 없습니다.", DidErrorCodes.ACCESS_DENIED);
    }

    // role이 USER인지 확인
    String role = getUserRoleOrThrowException();
    if (!GatewayHeaderConstants.Role.ROLE_USER.equals(role)) {
      throw new BusinessException("테넌트만 접근할 수 있습니다.", DidErrorCodes.PERMISSION_DENIED);
    }

    try {
      return Long.valueOf(accountId);
    } catch (NumberFormatException e) {
      throw new BusinessException("사용자 ID가 올바른 숫자 형식이 아닙니다: " + accountId,
          DidErrorCodes.ACCESS_DENIED);
    }
  }

  /**
   * Gateway에서 전달받은 테넌트 ID를 Long 타입으로 반환하거나 예외를 발생시킵니다. role이 TENANT인 경우에만 반환합니다.
   *
   * @return 테넌트 ID (Long)
   * @throws BusinessException 인증 정보가 없거나 숫자 형식이 아니거나 TENANT 권한이 아닌 경우
   */
  public static Long getTenantIdOrThrowException() {
    HttpServletRequest request = getCurrentRequest();
    String tenantId = request.getHeader(GatewayHeaderConstants.Auth.X_AUTH_ACCOUNT_ID);
    if (tenantId == null || tenantId.isEmpty()) {
      throw new BusinessException("테넌트 인증 정보가 없습니다.", DidErrorCodes.ACCESS_DENIED);
    }

    // role이 TENANT인지 확인
    String role = getUserRoleOrThrowException();
    if (!GatewayHeaderConstants.Role.ROLE_TENANT.equals(role)) {
      throw new BusinessException("테넌트만 접근할 수 있습니다.", DidErrorCodes.PERMISSION_DENIED);
    }

    try {
      return Long.valueOf(tenantId);
    } catch (NumberFormatException e) {
      throw new BusinessException("테넌트 ID가 올바른 숫자 형식이 아닙니다: " + tenantId,
          DidErrorCodes.ACCESS_DENIED);
    }
  }

  /**
   * Gateway에서 전달받은 관리자 ID를 Long 타입으로 반환하거나 예외를 발생시킵니다. role이 ADMIN인 경우에만 반환합니다.
   *
   * @return 관리자 ID (Long)
   * @throws BusinessException 인증 정보가 없거나 숫자 형식이 아니거나 ADMIN 권한이 아닌 경우
   */
  public static Long getAdminIdOrThrowException() {
    HttpServletRequest request = getCurrentRequest();
    String adminId = request.getHeader(GatewayHeaderConstants.Auth.X_AUTH_ACCOUNT_ID);
    if (adminId == null || adminId.isEmpty()) {
      throw new BusinessException("관리자 인증 정보가 없습니다.", DidErrorCodes.ACCESS_DENIED);
    }

    // role이 TENANT인지 확인
    String role = getUserRoleOrThrowException();
    if (!GatewayHeaderConstants.Role.ROLE_ADMIN.equals(role)) {
      throw new BusinessException("관리자만 접근할 수 있습니다.", DidErrorCodes.PERMISSION_DENIED);
    }

    try {
      return Long.valueOf(adminId);
    } catch (NumberFormatException e) {
      throw new BusinessException("관리자 ID가 올바른 숫자 형식이 아닙니다: " + adminId,
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
    String role = request.getHeader(GatewayHeaderConstants.Auth.X_AUTH_ROLE);
    if (role == null || role.isEmpty()) {
      throw new BusinessException("사용자 권한 정보가 없습니다.", DidErrorCodes.ACCESS_DENIED);
    }
    return role;
  }

  /**
   * Gateway에서 전달받은 클라이언트 디바이스 정보를 반환합니다.
   *
   * @return 클라이언트 디바이스
   * @throws BusinessException 디바이스 정보가 없는 경우
   */
  public static String getUserDeviceOrThrowException() {
    HttpServletRequest request = getCurrentRequest();
    String device = request.getHeader(GatewayHeaderConstants.Auth.X_AUTH_DEVICE_ID);
    if (device == null || device.isEmpty()) {
      throw new BusinessException("디바이스 ID를 찾을 수 없습니다", DidErrorCodes.ACCESS_DENIED);
    }
    return device;
  }

  /**
   * 사용자가 테넌트 권한을 가지고 있는지 확인합니다.
   *
   * @return 테넌트 권한 여부
   */
  public static boolean isTenant() {
    String role = getUserRoleOrThrowException();
    return GatewayHeaderConstants.Role.ROLE_TENANT.equals(role);
  }

  /**
   * 사용자가 일반 사용자 권한을 가지고 있는지 확인합니다.
   *
   * @return 일반 사용자 권한 여부
   */
  public static boolean isUser() {
    String role = getUserRoleOrThrowException();
    return GatewayHeaderConstants.Role.ROLE_USER.equals(role);
  }

  /**
   * Gateway에서 전달받은 사용자 ID를 반환합니다. role이 USER인 경우에만 반환합니다.
   *
   * @return 사용자 ID
   * @throws BusinessException role이 USER가 아닌 경우
   */
  public static Long getAccountIdOrThrow() {
    HttpServletRequest request = getCurrentRequest();
    String accountId = request.getHeader(GatewayHeaderConstants.Auth.X_AUTH_ACCOUNT_ID);
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
