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
public class TenantAccessAspect {

  @Around("@annotation(com.pyokemon.tenant.annotation.TenantOnly)")
  public Object checkTenantAccess(ProceedingJoinPoint joinPoint) throws Throwable {

    // Gateway에서 전달받은 권한 정보로 Tenant 체크
    GatewayRequestHeaderUtils.requireTenantRole();

    log.debug("Tenant access granted for method: {}", joinPoint.getSignature().getName());
    return joinPoint.proceed();
  }
}
