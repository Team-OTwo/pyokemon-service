package com.pyokemon.tenant.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.pyokemon.tenant.web.context.GatewayRequestHeaderUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminAccessAspect {

  // @AdminOnly 어노테이션이 붙은 메서드에 대한 권한 체크
  @Around("@annotation(com.pyokemon.tenant.annotation.AdminOnly)")
  public Object checkAdminAccess(ProceedingJoinPoint joinPoint) throws Throwable {

    // Gateway에서 전달받은 권한 정보로 Admin 체크
    GatewayRequestHeaderUtils.requireAdminRole();

    log.debug("Admin access granted for method: {}", joinPoint.getSignature().getName());
    return joinPoint.proceed();
  }
}
