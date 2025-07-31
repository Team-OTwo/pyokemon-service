package com.pyokemon.account.common.web.context;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.AccountErrorCodes;

public class GatewayRequestHeaderUtils {

  public static String getUserIdOrThrowException() {
    HttpServletRequest request = getCurrentRequest();
    String userId = (String) request.getAttribute("X-Auth-AccountId");
    if (userId == null || userId.isEmpty()) {
      throw new BusinessException("사용자 인증 정보가 없습니다.", AccountErrorCodes.ACCESS_DENIED);
    }
    return userId;
  }

  public static String getUserRoleOrThrowException() {
    HttpServletRequest request = getCurrentRequest();
    String role = (String) request.getAttribute("X-Auth-Role");
    if (role == null || role.isEmpty()) {
      throw new BusinessException("사용자 권한 정보가 없습니다.", AccountErrorCodes.ACCESS_DENIED);
    }
    return role;
  }

  private static HttpServletRequest getCurrentRequest() {
    ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attributes == null) {
      throw new BusinessException("요청 컨텍스트를 찾을 수 없습니다.", AccountErrorCodes.INTERNAL_ERROR);
    }
    return attributes.getRequest();
  }
}
