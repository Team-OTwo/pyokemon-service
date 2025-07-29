package com.pyokemon.account.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.pyokemon.account.common.annotation.AdminOnly;
import com.pyokemon.account.web.context.GatewayRequestHeaderUtils;
import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.AccountErrorCodes;

import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class AdminAccessAspect {

  @Around("@annotation(adminOnly)")
  public Object checkAdminAccess(ProceedingJoinPoint joinPoint, AdminOnly adminOnly)
      throws Throwable {
    try {
      String role = GatewayRequestHeaderUtils.getUserRoleOrThrowException();
      if (!"ADMIN".equals(role)) {
        throw new BusinessException(AccountErrorCodes.ACCESS_DENIED,
            AccountErrorCodes.ACCESS_DENIED);
      }
      return joinPoint.proceed();
    } catch (Exception e) {
      throw new BusinessException(AccountErrorCodes.ACCESS_DENIED, AccountErrorCodes.ACCESS_DENIED);
    }
  }
}
