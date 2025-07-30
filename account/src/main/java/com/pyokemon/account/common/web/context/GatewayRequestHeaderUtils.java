package com.pyokemon.account.common.web.context;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class GatewayRequestHeaderUtils {

  public static String getUserIdOrThrowException() {
    HttpServletRequest request = getCurrentRequest();
    String userId = (String) request.getAttribute("X-Auth-AccountId");
    if (userId == null || userId.isEmpty()) {
      throw new RuntimeException("User ID not found in request attribute");
    }
    return userId;
  }

  public static String getUserRoleOrThrowException() {
    HttpServletRequest request = getCurrentRequest();
    String role = (String) request.getAttribute("X-Auth-Role");
    if (role == null || role.isEmpty()) {
      throw new RuntimeException("User role not found in request attribute");
    }
    return role;
  }

  private static HttpServletRequest getCurrentRequest() {
    ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attributes == null) {
      throw new RuntimeException("No request context available");
    }
    return attributes.getRequest();
  }
}
