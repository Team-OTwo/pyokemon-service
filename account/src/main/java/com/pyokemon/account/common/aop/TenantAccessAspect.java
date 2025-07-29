package com.pyokemon.account.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.pyokemon.account.common.annotation.TenantOnly;
import com.pyokemon.account.web.context.GatewayRequestHeaderUtils;
import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.AccountErrorCodes;

import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class TenantAccessAspect {

  @Around("@annotation(tenantOnly)")
  public Object checkTenantAccess(ProceedingJoinPoint joinPoint, TenantOnly tenantOnly)
      throws Throwable {
    try {
      String role = GatewayRequestHeaderUtils.getUserRoleOrThrowException();
      if (!"TENANT".equals(role)) {
        throw new BusinessException(AccountErrorCodes.ACCESS_DENIED,
            AccountErrorCodes.ACCESS_DENIED);
      }
      return joinPoint.proceed();
    } catch (Exception e) {
      throw new BusinessException(AccountErrorCodes.ACCESS_DENIED, AccountErrorCodes.ACCESS_DENIED);
    }
  }
}
